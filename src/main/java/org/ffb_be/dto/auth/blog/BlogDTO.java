package org.ffb_be.dto.auth.blog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.auth.comment.CommentDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogDTO {
    private Long id;
    private String title;
    private String content;
    private List<String> imageUrls;
    private CommentDTO latestComment;
    private List<CommentDTO> comments;
}
