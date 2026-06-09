package com.recaudo.api.domain.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dashboard_nopago")
public class DashboardNoPagoEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "value", nullable = false)
        private BigDecimal value;

        @Column(name = "zona_id")
        private Long zonaId;

        @Column(name = "user_create", length = 255)
        private String userCreate;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "cant")
        private Integer cant;

}
