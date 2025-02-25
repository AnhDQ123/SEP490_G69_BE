package org.ffb_be.service.blog;

import org.ffb_be.entity.Blog;

import java.util.List;

public interface BlogService {

    Blog createBlog(Blog blog);

    Blog getBlogById(Long id);

    Blog updateBlog(Long id, Blog blogDetails);

    void deleteBlog(Long id);

    List<Blog> getBlogs(int page, int limit);
}
