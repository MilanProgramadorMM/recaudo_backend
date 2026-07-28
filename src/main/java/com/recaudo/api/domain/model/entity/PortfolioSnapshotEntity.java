package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_snapshot",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_snapshot_credit_date",
           columnNames = {"snapshot_date", "credit_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "credit_id", nullable = false)
    private Integer creditId;

    @Column(name = "person_id", nullable = false)
    private Integer personId;

    @Column(name = "cliente_fullname")
    private String clienteFullname;

    @Column(name = "cliente_documento")
    private String clienteDocumento;

    @Column(name = "zona_id")
    private Long zonaId;

    @Column(name = "zona_nombre")
    private String zonaNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_credito")
    private CreditStatus estadoCredito;

    @Column(name = "credit_line_id")
    private Long creditLineId;

    @Column(name = "credit_line_nombre")
    private String creditLineNombre;

    @Column(name = "period_id")
    private Long periodId;

    @Column(name = "period_nombre")
    private String periodNombre;

    @Column(name = "period_codigo")
    private String periodCodigo;

    @Column(name = "tax_type_id")
    private Long taxTypeId;

    @Column(name = "tax_type_nombre")
    private String taxTypeNombre;

    @Column(name = "tax_value")
    private BigDecimal taxValue;

    @Column(name = "cuotas_planeadas")
    private Integer cuotasPlaneadas;

    @Column(name = "total_cuotas")
    private Integer totalCuotas;

    @Column(name = "cuotas_pagadas")
    private Integer cuotasPagadas;

    @Column(name = "cuotas_pendientes")
    private Integer cuotasPendientes;

    @Column(name = "capital_generado")
    private BigDecimal capitalGenerado;

    @Column(name = "capital_pagado")
    private BigDecimal capitalPagado;

    @Column(name = "capital_pendiente")
    private BigDecimal capitalPendiente;

    @Column(name = "interes_generado")
    private BigDecimal interesGenerado;

    @Column(name = "interes_pagado")
    private BigDecimal interesPagado;

    @Column(name = "interes_pendiente")
    private BigDecimal interesPendiente;

    @Column(name = "seguro_vida_generado")
    private BigDecimal seguroVidaGenerado;

    @Column(name = "seguro_vida_pagado")
    private BigDecimal seguroVidaPagado;

    @Column(name = "seguro_vida_pendiente")
    private BigDecimal seguroVidaPendiente;

    @Column(name = "seguro_cartera_generado")
    private BigDecimal seguroCarteraGenerado;

    @Column(name = "seguro_cartera_pagado")
    private BigDecimal seguroCarteraPagado;

    @Column(name = "seguro_cartera_pendiente")
    private BigDecimal seguroCarteraPendiente;

    @Column(name = "mora_generada")
    private BigDecimal moraGenerada;

    @Column(name = "mora_pagada")
    private BigDecimal moraPagada;

    @Column(name = "mora_pendiente")
    private BigDecimal moraPendiente;

    @Column(name = "dias_mora")
    private Integer diasMora;

    @Column(name = "total_pagado")
    private BigDecimal totalPagado;

    @Column(name = "saldo_total")
    private BigDecimal saldoTotal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "job_execution_id")
    private String jobExecutionId;

    @Column(name = "otros_conceptos_generado")
    private BigDecimal otrosConceptosGenerado;

    @Column(name = "rating_value")
    private String ratingValue;

    @Column(name = "rating_range_start")
    private Integer ratingRangeStart;

    @Column(name = "rating_range_end")
    private Integer ratingRangeEnd;

    public enum CreditStatus { ACTIVE, CANCELLED, INACTIVE }
}