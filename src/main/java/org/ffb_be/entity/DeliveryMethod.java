package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.CartEnum;
import org.ffb_be.utils.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "delivery_method")
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryMethod {
    @Id
    @Column(name = "delivery_method_id")
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

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;
}
