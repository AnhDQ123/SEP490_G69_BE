package org.ffb_be.service.blog;

import org.ffb_be.dto.blog.BlogDTO;

import java.util.List;

public interface BlogService {
    List<BlogDTO> getBlogs(int page, int limit);

    BlogDTO getBlogById(Long id);

    void createBlog(BlogDTO blogDTO);

    void updateBlog(Long id, BlogDTO blogDTO);

    void deleteBlog(Long id);
}
