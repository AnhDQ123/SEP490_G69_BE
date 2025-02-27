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
@Table(name = "vouchers")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Voucher  extends BaseEntity {
    @Id
    @Column(name = "voucher_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(name="discount_percentage")
    private BigDecimal discount_percentage;

    @Column(name="start_date")
    private LocalDate start_date;

    @Column(name="end_date")
    private LocalDate end_date ;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "voucher")
    private List<Order> orders;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;
}
