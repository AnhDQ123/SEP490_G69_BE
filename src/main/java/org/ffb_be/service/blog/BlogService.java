package org.ffb_be.service.blog;

import org.ffb_be.dto.blog.BlogDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BlogService {
    List<BlogDTO> getBlogs(int page, int limit);

    BlogDTO getBlogById(Long id);

    void createBlog(BlogDTO blogDTO, MultipartFile[] files) throws IOException;

    void updateBlog(Long id, BlogDTO blogDTO, MultipartFile[] files) throws IOException;

    void toggleLike(Long blogId, Long userId);

    void deleteBlog(Long id);
}
