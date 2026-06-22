package com.recaudo.api.domain.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dashboard_recaudo")
public class DashboardRecaudoEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "value", nullable = false)
        private BigDecimal value;

        @Column(name = "zona_id")
        private Long zonaId;

        @Column(name = "user_create", length = 255)
        private String userCreate;

        @Column(name = "zona_code", length = 255)
        private String zonCode;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "create_recaudo", updatable = false)
        private LocalDate createRecaudo;
}
