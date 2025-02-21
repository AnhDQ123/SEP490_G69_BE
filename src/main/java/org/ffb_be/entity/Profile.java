package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Entity
@Table(name = "profiles")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile extends BaseEntity {
    @Id
    @Column(name = "profile_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="fullname")
    private String name;

    @Column(name="address")
    private String address;

    @Column(name="avatar")
    private String avatar;

    @Column(name="tax_code")
    private String tax_code;

    @Column(name="citizenIDNumber")
    private String citizenIDNumber;

    @Column(name="citizen_id_card_front")
    private String citizenIDCardFront;

    @Column(name="citizen_id_card_back")
    private String citizenIDCardBack;

    @Column(name="driving_license_front")
    private String drivingLicenseFront;

    @Column(name="driving_license_back")
    private String drivingLicenseBack;

    @Column(name="citizen_id_expired_date")
    private LocalDate citizenIDExpiredDate;

    @Column(name="driving_license_date")
    private LocalDate drivingLicenseDate;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

}
