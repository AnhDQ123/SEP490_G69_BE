package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.ConfigCategory;
import org.ffb_be.utils.enums.Status;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "configs")
public class Config extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfigCategory category;

    @Column(name = "config_key", nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;
}
