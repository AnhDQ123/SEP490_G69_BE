package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "images")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Image extends BaseEntity {
    @Id
    @Column(name = "image_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url")
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    private Types type;

    private Long relatedId;
}
