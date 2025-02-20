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
@Table(name = "delivery_methods")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryMethod extends BaseEntity{
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

    @OneToMany(mappedBy = "deliveryMethod")
    private List<Order> orders;
}
