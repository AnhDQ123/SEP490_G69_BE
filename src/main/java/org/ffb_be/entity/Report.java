package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.ReportStatus;

@Entity
@Table(name = "reports")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Report extends BaseEntity {
    @Id
    @Column(name = "report_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private Types type;

    private Long relatedId;
}
