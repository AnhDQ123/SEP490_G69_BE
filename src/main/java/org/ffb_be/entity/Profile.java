package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDate;
@Entity
@Table(name = "types")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile {
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

    @Column(name="citizenIDCardFront")
    private String citizenIDCardFront;

    @Column(name="citizenIDCardBack")
    private String citizenIDCardBack;

    @Column(name="drivingLicenseFront")
    private String drivingLicenseFront;

    @Column(name="drivingLicenseBack")
    private String drivingLicenseBack;

    @Column(name="citizenIDExpiredDate")
    private LocalDate citizenIDExpiredDate;

    @Column(name="drivingLicenseDate")
    private LocalDate drivingLicenseDate;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

}
