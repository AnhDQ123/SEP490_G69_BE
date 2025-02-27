package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "payment_methods")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment extends BaseEntity {
    @Id
    @Column(name = "payment_method_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="method_name")
    private String name;

    @Column(name="description")
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name="fee")
    private BigDecimal fee;

    @OneToMany(mappedBy = "paymentMethod")
    private List<Order> orders;
}
