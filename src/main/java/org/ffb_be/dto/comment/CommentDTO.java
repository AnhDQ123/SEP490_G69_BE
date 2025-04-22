package org.ffb_be.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.auth.userDto.WriterDTO;
import org.ffb_be.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO{
    private Long id;
    private String content;
    private WriterDTO writer;
    private Long parentId;
    private String parentWriterName;
    private int likeCount;
    private int replyCount;
    private boolean hasMoreReplies;
    private LocalDateTime createdAt;
}
