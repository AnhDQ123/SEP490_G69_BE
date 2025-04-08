package org.ffb_be.service.blog;

import org.ffb_be.dto.blog.BlogDTO;
import org.ffb_be.utils.enums.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BlogService {
    List<BlogDTO> getBlogs(Pageable pageable, boolean isOperator);

    BlogDTO getBlogById(Long id);

    void createBlog(BlogDTO blogDTO, MultipartFile[] files) throws IOException;

    void updateBlog(Long id, BlogDTO blogDTO, MultipartFile[] files) throws IOException;

    void toggleLike(Long blogId, Long userId);

    void deleteBlog(Long id);

    void blogStatusUpdate(Long id, Status status, String reason);
}
