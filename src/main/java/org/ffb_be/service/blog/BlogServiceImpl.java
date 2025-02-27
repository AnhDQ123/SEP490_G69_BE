package org.ffb_be.service.blog;

import lombok.AllArgsConstructor;
import org.ffb_be.dto.blog.BlogDTO;
import org.ffb_be.entity.Blog;
import org.ffb_be.entity.Comment;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class BlogServiceImpl implements BlogService {
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
                .orElseThrow(() -> new NotFoundException("Blog"));
        return mapBlogToDTO(blog);
    }

    private BlogDTO mapBlogToDTO(Blog blog) {
        List<Image> images = imageRepository.findBlogImagesByRelatedId(blog.getId());
        List<Comment> rootComments = commentRepository.findRootCommentsByBlogId(blog.getId());
        return blogMapper.toDTOWithImagesAndComments(blog, images, rootComments);
    }

    @Override
    public void createBlog(BlogDTO blogDTO) {
        Blog blog = blogMapper.toEntity(blogDTO);
        blog = blogRepository.save(blog);

        List<Comment> comments = blogMapper.toEntities(blogDTO.getComments(), blog);
        commentRepository.saveAll(comments);

        saveBlogImages(blog.getId(), blogDTO.getImageUrls());
    }

    @Override
    public void updateBlog(Long blogId, BlogDTO blogDTO) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog"));

        blogMapper.updateEntity(blogDTO, blog);
        blogRepository.save(blog);

        imageRepository.deleteByBlogId(blogId);
        saveBlogImages(blogId, blogDTO.getImageUrls());
    }

    private void saveBlogImages(Long blogId, List<String> imageUrls) {
        Types blogType = typesRepository.findByCategory(TypesCategory.BLOG)
                .orElseThrow(() -> new NotFoundException("Types"));

        Set<String> existingUrls = imageRepository.findBlogImagesByRelatedId(blogId)
                .stream()
                .map(Image::getUrl)
                .collect(Collectors.toSet());

        List<Image> imagesToAdd = imageUrls.stream()
                .filter(url -> !existingUrls.contains(url))
                .limit(5)
                .map(url -> new Image(null, url, blogType, blogId))
                .collect(Collectors.toList());
        imageRepository.saveAll(imagesToAdd);
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
