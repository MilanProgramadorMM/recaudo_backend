package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "credit_intention_disbursement")
public class CreditIntentionDisbursementEntity extends BasePaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_intention_id")
    private Long creditIntentionId;

    @Column(name = "payment_type_id")
    private Long paymentTypeId;

    @Column(name = "bank_id")
    private Long bankId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size")
    private Long fileSize;

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "user_edit")
    private String userEdit;

    @Column(name = "user_delete")
    private String userDelete;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "status")
    private Boolean status = true;

}
