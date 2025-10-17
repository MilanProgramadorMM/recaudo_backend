package com.recaudo.api.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "person_zona")
public class PersonZonaEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "zona_id")
    private Long zonaId;

    @Column(name = "orden")
    private Long orden;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "status")
    private boolean status = true;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;


}
