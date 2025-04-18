package org.ffb_be.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.auth.userDto.WriterDTO;
import org.ffb_be.entity.BaseEntity;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO extends BaseEntity {
    private Long id;
    private String content;
    private WriterDTO writer;
    private Long parentId;
    private String parentWriterName;
    private int replyCount;
    private boolean hasMoreReplies;
}
