package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
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
@Table(name = "credit_line")
public class CreditLineEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "min_quota")
    private BigDecimal minQuota;

    @Column(name = "max_quota")
    private BigDecimal maxQuota;

    @Column(name = "min_period")
    private Integer minPeriod;

    @Column(name = "max_period")
    private Integer maxPeriod;

    @Column(name = "tax_type_id")
    private Long taxType;

    @Column(name = "amortization_type_id")
    private Long amortizationType;

    @Column(name = "procedure_name")
    private String procedureName;

    @Column(name = "life_insurance")
    private boolean lifeInsurance;

    @Column(name = "portfolio_insurance")
    private boolean portfolioInsurance;

    @Column(name = "require_documentation")
    private boolean requireDocumentation;

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
