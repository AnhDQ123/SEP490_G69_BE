package org.ffb_be.service.blog;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.blog.BlogDTO;
import org.ffb_be.entity.Blog;
import org.ffb_be.entity.Image;
import org.ffb_be.entity.Types;
import org.ffb_be.entity.User;
import org.ffb_be.exception.BadRequestException;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.*;
import org.ffb_be.utils.enums.Status;
import org.ffb_be.utils.enums.TypesCategory;
import org.ffb_be.utils.enums.upload.CloudinaryUpload;
import org.ffb_be.utils.mapping.BlogMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {
    private final BlogRepository blogRepository;
    private final ImageRepository imageRepository;
    private final CommentRepository commentRepository;
    private final TypesRepository typesRepository;
    private final UserRepository userRepository;
    private final BlogMapper blogMapper;
    private final CloudinaryUpload cloudinaryUpload;

    @Override
    public List<BlogDTO> getBlogs(Pageable pageable) {
        Page<Blog> blogPage = blogRepository.findAll(pageable);
        return blogPage.getContent().stream()
                .map(this::mapBlogToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BlogDTO getBlogById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        return mapBlogToDTO(blog);
    }

    private BlogDTO mapBlogToDTO(Blog blog) {
        List<Image> images = imageRepository.findBlogImagesByRelatedId(blog.getId());
        int commentCount = commentRepository.countByBlogId(blog.getId()); // Đếm số comment
        BlogDTO blogDTO = blogMapper.toDTOWithImages(blog, images);
        blogDTO.setWriter(blogMapper.toWriterDTO(blog.getWriter()));
        blogDTO.setCommentCount(commentCount);
        return blogDTO;
    }

    @Override
    public void createBlog(BlogDTO blogDTO, MultipartFile[] files) throws IOException {
        if (blogDTO.getContent() == null && (files == null || files.length == 0)) {
            throw new RuntimeException("Content or file is required to create a blog");
        }
        Blog blog = blogMapper.toEntity(blogDTO);
        if (blogDTO.getWriter() != null) {
            User writer = userRepository.findById(blogDTO.getWriter().getId())
                    .orElseThrow(() -> new NotFoundException("User"));
            blog.setWriter(writer);
        }

        blog.setStatus(Status.ACTIVE);
        blog.setCreatedAt(LocalDateTime.now());

        blog = blogRepository.save(blog);
        if(files != null) {
            Types blogType = typesRepository.findByCategory(TypesCategory.BLOG)
                    .orElseThrow(() -> new NotFoundException("Types"));
            for(String url : cloudinaryUpload.uploadFiles(files)) {
                imageRepository.saveBlogImages(url, blogType.getId(), blog.getId());
            }
        }
    }

    @Override
    public void updateBlog(Long blogId, BlogDTO blogDTO, MultipartFile[] files) throws IOException {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog"));

        blogMapper.updateEntity(blogDTO, blog);
        blogRepository.save(blog);

        if(files != null) {
            // Nếu có file upload, upload lên Cloudinary
            List<String> uploadedUrls = (files.length > 0) ? cloudinaryUpload.uploadFiles(files) : new ArrayList<>();

            // Gộp URL cũ từ request và URL mới từ file upload
            List<String> finalImageUrls = new ArrayList<>(uploadedUrls);
            if (blogDTO.getImageUrls() != null) {
                finalImageUrls.addAll(blogDTO.getImageUrls());
            }


            updateBlogImages(blogId, finalImageUrls);
        }
    }

    private void updateBlogImages(Long blogId, List<String> imageUrls) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog"));
        List<String> existingUrls = imageRepository.findImageUrlsByBlogId(blogId);

        // Tìm ảnh cần xóa (có trong DB nhưng không có trong danh sách mới)
        List<String> urlsToDelete = existingUrls.stream()
                .filter(url -> !imageUrls.contains(url))
                .collect(Collectors.toList());

        // Xóa ảnh cũ khỏi Cloudinary
        cloudinaryUpload.deleteImagesFromCloudinary(urlsToDelete);

        // Xóa ảnh cũ khỏi database
        imageRepository.deleteByBlogIdAndUrlNotIn(blogId, imageUrls);

        // Thêm ảnh mới
        Types blogType = typesRepository.findByCategory(TypesCategory.BLOG)
                .orElseThrow(() -> new NotFoundException("Types"));

        List<Image> newImages = imageUrls.stream()
                .filter(url -> !existingUrls.contains(url)) // Chỉ thêm ảnh chưa có
                .limit(5)
                .map(url -> new Image(null, url, blogType, blog.getWriter().getId(), blogId, Status.ACTIVE))
                .collect(Collectors.toList());

        if (!newImages.isEmpty()) {
            imageRepository.saveAll(newImages);
        }
    }

    @Override
    public void toggleLike(Long blogId, Long userId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog not found"));

        Set<Long> likedUserSet = new HashSet<>();
        if (blog.getLikedUsers() != null) {
            likedUserSet.addAll(Arrays.stream(blog.getLikedUsers().split(","))
                    .map(Long::parseLong).collect(Collectors.toSet()));
        }

        if (likedUserSet.contains(userId)) {
            likedUserSet.remove(userId);
            blog.setLikeCount(blog.getLikeCount() - 1);
        } else {
            likedUserSet.add(userId);
            blog.setLikeCount(blog.getLikeCount() + 1);
        }

        blog.setLikedUsers(likedUserSet.isEmpty() ? null : likedUserSet.stream()
                .map(String::valueOf).collect(Collectors.joining(",")));

        blogRepository.save(blog);
    }

    @Override
    public void deleteBlog(Long id) {
        if (!blogRepository.existsById(id)) {
            throw new NotFoundException("Blog");
        }
        commentRepository.deleteByBlogId(id);
        imageRepository.deleteByBlogId(id);
        blogRepository.deleteById(id);
    }

    @Override
    public void blogStatusUpdate(Long id, Status status, String reason) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog"));
        if (blog.getStatus().equals(status)) {
            throw new BadRequestException("Blog đã ở trạng thái này rồi");
        }
        if (status == Status.ACTIVE&&!blog.getStatus().equals(Status.ACTIVE)) {
            blog.setReason(null);
        }
        if (status == Status.INACTIVE&&!blog.getStatus().equals(Status.INACTIVE)) {
            blog.setReason(reason);
        }
        blog.setStatus(status);
        blogRepository.save(blog);
    }
}
