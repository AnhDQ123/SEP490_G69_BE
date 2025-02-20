package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.TypesEnum;

@Entity
@Table(name = "types")
@AllArgsConstructor
@NoArgsConstructor
public class Types {
    @Id
    @Column(name = "type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypesEnum status;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

}
