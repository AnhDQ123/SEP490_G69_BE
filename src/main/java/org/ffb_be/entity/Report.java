package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.ReportEnum;
import org.ffb_be.utils.enums.Status;

import java.time.LocalDate;

@Entity
@Table(name = "report")
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    @Id
    @Column(name = "report")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    private ReportEnum status;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;

}
