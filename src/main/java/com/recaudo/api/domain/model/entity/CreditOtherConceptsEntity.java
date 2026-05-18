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
@Table(name = "credit_other_concepts")
public class CreditOtherConceptsEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "credit_id", nullable = false)
        private Long creditId;

        @Column(name = "quota_number", nullable = false)
        private Integer quotaNumber;

        @Column(name = "expiration_date", nullable = false)
        private LocalDate expirationDate;

        @Column(name = "total_quota_value", nullable = false, precision = 15, scale = 2)
        private BigDecimal totalQuotaValue;

        @Column(name = "liquidated", length = 1)
        @Builder.Default
        private String liquidated = "N";

        /** 'S' = la cuota fue pagada completamente */
        @Column(name = "paid_full", length = 1)
        @Builder.Default
        private String paidFull = "N";



}
