package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;

import java.time.LocalDate;

@Entity
@Table(name = "feedback")
@AllArgsConstructor
@NoArgsConstructor
public class Feedback {
    @Id
    @Column(name = "feedback_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="content")
    private String content;

    @Column(name="rate")
    private int rate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;
}
