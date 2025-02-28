package org.ffb_be.service.blog;

import lombok.RequiredArgsConstructor;
import org.ffb_be.dto.blog.BlogDTO;
import org.ffb_be.entity.Blog;
import org.ffb_be.entity.Image;
import org.ffb_be.entity.Types;
import org.ffb_be.exception.NotFoundException;
import org.ffb_be.repository.BlogRepository;
import org.ffb_be.repository.CommentRepository;
import org.ffb_be.repository.ImageRepository;
import org.ffb_be.repository.TypesRepository;
import org.ffb_be.utils.enums.TypesCategory;
import org.ffb_be.utils.mapping.BlogMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
class BlogServiceImpl implements BlogService {
    private final BlogRepository blogRepository;
    private final ImageRepository imageRepository;
    private final CommentRepository commentRepository;
    private final TypesRepository typesRepository;
    private final BlogMapper blogMapper;

    @Override
    public List<BlogDTO> getBlogs(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by("id").ascending());
        Page<Blog> blogPage = blogRepository.findAll(pageable);
        return blogPage.getContent().stream()
                .map(this::mapBlogToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BlogDTO getBlogById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));

        List<Image> images = imageRepository.findBlogImagesByRelatedId(blog.getId());
        return blogMapper.toDTOWithImages(blog, images);
    }

    private BlogDTO mapBlogToDTO(Blog blog) {
        List<Image> images = imageRepository.findBlogImagesByRelatedId(blog.getId());
        int commentCount = commentRepository.countByBlogId(blog.getId()); // Đếm số comment
        BlogDTO blogDTO = blogMapper.toDTOWithImages(blog, images);
        blogDTO.setCommentCount(commentCount);
        return blogDTO;
    }

    @Override
    public void createBlog(BlogDTO blogDTO) {
        Blog blog = blogMapper.toEntity(blogDTO);
        blog = blogRepository.save(blog);
        Types blogType = typesRepository.findByCategory(TypesCategory.BLOG)
                .orElseThrow(() -> new NotFoundException("Types"));
        imageRepository.saveBlogImages(blog.getId(), blogDTO.getImageUrls(),blogType);
    }

    @Override
    public void updateBlog(Long blogId, BlogDTO blogDTO) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog"));

        blogMapper.updateEntity(blogDTO, blog);
        blogRepository.save(blog);

        // Cập nhật hình ảnh
        updateBlogImages(blogId, blogDTO.getImageUrls());
    }

    private void updateBlogImages(Long blogId, List<String> imageUrls) {
        Set<String> existingUrls = new HashSet<>(imageRepository.findImageUrlsByBlogId(blogId));

        // Xóa ảnh không còn tồn tại trong danh sách mới
        imageRepository.deleteByBlogIdAndUrlNotIn(blogId, imageUrls);

        // Thêm ảnh mới nếu chưa tồn tại
        Types blogType = typesRepository.findByCategory(TypesCategory.BLOG)
                .orElseThrow(() -> new NotFoundException("Types"));

        List<Image> newImages = imageUrls.stream()
                .filter(url -> !existingUrls.contains(url))
                .limit(5)
                .map(url -> new Image(null, url, blogType, blogId))
                .collect(Collectors.toList());

        if (!newImages.isEmpty()) {
            imageRepository.saveAll(newImages);
        }
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
}
