package org.ffb_be.service.comment;

import org.ffb_be.dto.comment.CommentDTO;

import java.util.List;

public interface CommentService {
    // 📌 Lấy danh sách comment gốc của blog
    List<CommentDTO> getCommentsByBlogId(Long blogId);

    // 📌 Lấy thêm reply của một comment
    List<CommentDTO> getMoreReplies(Long parentId);

    // 📌 Thêm mới một comment
    void addComment(Long blogId, CommentDTO commentDTO);

    // 📌 Xóa comment
    void deleteComment(Long commentId);
}
