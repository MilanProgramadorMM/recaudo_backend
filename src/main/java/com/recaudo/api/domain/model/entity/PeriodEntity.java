package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "period")
public class PeriodEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod", length = 3, nullable = false, unique = true)
    private String cod;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "conversion_factor", nullable = false)
    private Integer factorConversion;

    @Builder.Default
    @Column(name = "status")
    private boolean status = true;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "user_delete")
    private String userDelete;

    @Column(name = "user_edit")
    private String userEdit;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;
}
