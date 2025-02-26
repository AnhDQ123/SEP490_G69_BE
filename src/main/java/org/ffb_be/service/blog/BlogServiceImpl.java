package org.ffb_be.service.blog;

import org.ffb_be.entity.Blog;
import org.ffb_be.repository.BlogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BlogServiceImpl implements BlogService{
    private final BlogRepository blogRepository;

    public BlogServiceImpl(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    @Override
    public Blog createBlog(Blog blog) {
        return null;
    }

    public List<Blog> getBlogs(int page, int limit) {
        return blogRepository.findBlogs(PageRequest.of(page, limit));
    }

    @Override
    public Blog getBlogById(Long id) {
        return null;
    }

    @Override
    public Blog updateBlog(Long id, Blog blogDetails) {
        return null;
    }

    @Override
    public void deleteBlog(Long id) {

    }
}
