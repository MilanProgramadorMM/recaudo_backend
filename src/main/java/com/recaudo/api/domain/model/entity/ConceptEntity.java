package com.recaudo.api.domain.model.entity;

import com.recaudo.api.infrastructure.helper.util.ConceptNature;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "concept")
public class ConceptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concept_key", length = 5, nullable = false, unique = true)
    private String conceptKey;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature", nullable = false)
    private ConceptNature nature;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
