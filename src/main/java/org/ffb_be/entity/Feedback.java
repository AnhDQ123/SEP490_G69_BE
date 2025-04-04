package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;


@Entity
@Table(name = "feedbacks")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Feedback extends BaseEntity{
    @Id
    @Column(name = "feedback_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="content" ,columnDefinition = "TEXT")
    private String content;

    @Column(name="rate")
    private Double rate;

    @Column
    private String image;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User writer;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;
}
