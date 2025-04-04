package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "delivery_methods")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DeliveryMethod extends BaseEntity{
    @Id
    @Column(name = "delivery_method_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="method_name")
    private String name;

    @Column(name="description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name="fee")
    private BigDecimal fee;

    @OneToMany(mappedBy = "deliveryMethod")
    private List<Order> orders;
}
