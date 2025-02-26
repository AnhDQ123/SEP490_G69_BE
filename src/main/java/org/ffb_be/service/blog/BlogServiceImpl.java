package org.ffb_be.service.blog;

import lombok.AllArgsConstructor;
import org.ffb_be.dto.auth.blog.BlogDTO;
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
        Pageable pageable = PageRequest.of(page, limit, Sort.by("id").descending());
        Page<Blog> blogPage = blogRepository.findAll(pageable);
        return blogPage.getContent().stream()
                .map(blog -> {
                    List<Image> images = imageRepository.findBlogImagesByRelatedId(blog.getId());
                    Comment topComment = commentRepository.findTopByBlogIdOrderByCreatedAtDesc(blog.getId());
                    return blogMapper.toDTOWithImagesAndComment(blog, images, topComment);
                })
                .collect(Collectors.toList());
    }

    @Override
    public BlogDTO getBlogById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog"));

        List<Image> images = imageRepository.findBlogImagesByRelatedId(id);
        List<Comment> comments = commentRepository.findCommentsByBlogId(id);

        return blogMapper.toDTOWithFullComments(blog, images, comments);
    }

    @Override
    public void createBlog(BlogDTO blogDTO) {
        Blog blog = blogMapper.toEntity(blogDTO);
        Blog savedBlog = blogRepository.save(blog);

        List<Comment> comments = blogMapper.toEntities(blogDTO.getComments(), savedBlog);
        commentRepository.saveAll(comments);

        saveBlogImages(savedBlog.getId(), blogDTO.getImageUrls());
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

        List<Image> images = imageUrls.stream()
                .limit(5)
                .map(url -> new Image(null, url, blogType, blogId))
                .collect(Collectors.toList());
        imageRepository.saveAll(images);
    }

    @Override
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog"));
        commentRepository.deleteByBlogId(id);
        imageRepository.deleteByBlogId(id);
        blogRepository.delete(blog);
    }
}