package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "new_recaudo_detail")
public class RecaudoDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recaudo_id", nullable = false)
    private Long recaudoId;

    @Column(name = "type_recaudo_id", nullable = false)
    private Long typeRecaudoId;

    @Column(name = "concept_id", nullable = false)
    private Long conceptId;

    @Column(name = "value", nullable = false, precision = 15, scale = 2)
    private BigDecimal value;
}