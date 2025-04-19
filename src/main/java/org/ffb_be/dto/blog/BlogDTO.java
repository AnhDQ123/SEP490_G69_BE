package org.ffb_be.dto.blog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.dto.auth.userDto.WriterDTO;
import org.ffb_be.entity.BaseEntity;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogDTO extends BaseEntity {
    private Long id;
    private String content;
    private WriterDTO writer;
    private int likeCount;
    private List<String> imageUrls;
    private int commentCount;
}
