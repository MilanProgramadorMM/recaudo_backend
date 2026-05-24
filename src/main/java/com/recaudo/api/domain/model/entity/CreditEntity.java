package com.recaudo.api.domain.model.entity;

import com.recaudo.api.domain.model.constant.CreditStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit")
public class CreditEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_intention_id", nullable = false)
    private Long creditIntentionId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "credit_line_id", nullable = false)
    private Long creditLineId;

    @Column(name = "quota_value", precision = 10, scale = 2)
    private BigDecimal quotaValue;

    @Column(name = "period_id")
    private Long periodId;

    @Column(name = "period_quantity")
    private Integer periodQuantity;

    @Column(name = "tax_type_id")
    private Long taxTypeId;

    @Column(name = "tax_value", precision = 10, scale = 2)
    private BigDecimal taxValue;

    @Column(name = "total_intention_value", precision = 10, scale = 2)
    private BigDecimal totalIntentionValue;

    @Column(name = "total_interest_value", precision = 10, scale = 2)
    private BigDecimal totalInterestValue;

    @Column(name = "total_capital_value", precision = 10, scale = 2)
    private BigDecimal totalCapitalValue;

    @Column(name = "item_value", precision = 10, scale = 2)
    private BigDecimal itemValue;

    @Column(name = "initial_value_payment", precision = 10, scale = 2)
    private BigDecimal initialValuePayment;

    @Column(name = "total_financed_value", precision = 10, scale = 2)
    private BigDecimal totalFinancedValue;

    @Column(name = "user_create", length = 50)
    private String userCreate;

    @Column(name = "user_delete", length = 50)
    private String userDelete;

    @Column(name = "user_edit", length = 50)
    private String userEdit;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "stationery", precision = 10, scale = 2)
    private BigDecimal stationery;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_status", length = 20)
    private CreditStatus creditStatus = CreditStatus.ACTIVE;
}