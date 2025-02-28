package org.ffb_be.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.entity.BaseEntity;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO extends BaseEntity {
    private Long id;
    private String content;
//    private String author;
    private Long parentId;
    private List<CommentDTO> replies;
    private boolean hasMoreReplies;
}
