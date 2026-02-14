package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_line_service_quota")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditLineServiceQuotaEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_line_id")
    private Long creditLineId;

    @Column(name = "service_quota_id")
    private Long serviceQuotaId;

    @Column(name = "capitalize")
    private Boolean capitalize;

    @Column(name = "defers")
    private Boolean defers;

    @Column(name = "received")
    private Boolean received;

    @Column(name = "discount_disbursement")
    private Boolean discount_disbursement;

    @Column(name = "allow_modification")
    private Boolean allow_modification;

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
