package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recaudo")
public class RecaudoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_id", nullable = false)
    private Long creditId;

    @Column(name = "cuota_id")
    private Long cuotaId;

    //@Column(name = "concept_id", nullable = false)
    @Column(name = "concept_id")
    private Long conceptId;

    //@Column(name = "value_paid", nullable = false)
    @Column(name = "value_paid")
    private BigDecimal valuePaid;

    @Column(name = "investment_value")
    private BigDecimal investmentValue;

    @Column(name = "interest_value")
    private BigDecimal interestValue;

    @Column(name = "life_insurance")
    private BigDecimal lifeInsurance;

    @Column(name = "portfolio_insurance")
    private BigDecimal portfolioInsurance;

    //@Column(name = "payment_type_id", nullable = false)
    @Column(name = "payment_type_id")
    private Long paymentTypeId;

    @Column(name = "bank_id")
    private Long bankId;

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size")
    private Long size;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "user_edit")
    private String userEdit;

    @Column(name = "user_delete")
    private String userDelete;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "delay_penalty")
    private BigDecimal delayPenalty;
}
