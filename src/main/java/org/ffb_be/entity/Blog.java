package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ffb_be.utils.enums.Status;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blogs")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Blog extends BaseEntity{
    @Id
    @Column(name = "blog_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="content", nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}
