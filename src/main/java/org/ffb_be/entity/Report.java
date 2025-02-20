package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.ReportStatus;

import java.time.LocalDate;

@Entity
@Table(name = "reports")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Report {
    @Id
    @Column(name = "report")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private Types type;

    private Long relatedId;
}
