package org.ffb_be.service.blog;

import lombok.AllArgsConstructor;
import org.ffb_be.entity.Blog;
import org.ffb_be.repository.BlogRepository;
import org.ffb_be.service.comment.CommentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class BlogServiceImpl implements BlogService{
    private final BlogRepository blogRepository;
    private final CommentService commentService;

    @Override
    public Blog createBlog(Blog blog) {
        return blogRepository.save(blog);
    }

    public List<Blog> getBlogs(int page, int limit) {
        return blogRepository.findBlogs(PageRequest.of(page, limit));
    }

    @Override
    public Blog getBlogById(Long id) {
        return blogRepository.findById(id).orElse(null);
    }

    @Override
    public Blog updateBlog(Long id, Blog blogDetails) {
        return null;
    }

    @Override
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }
}
