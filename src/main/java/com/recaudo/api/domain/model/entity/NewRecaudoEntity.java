package com.recaudo.api.domain.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "new_recaudo")
public class NewRecaudoEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "credit_id")
        private Long creditId;

        @Column(name = "quota_id")
        private Long quotaId;

        @Column(name = "value_paid", nullable = false)
        private BigDecimal valuePaid;

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

        @Column(name = "user_create", length = 255)
        private String userCreate;

        @Column(name = "user_edit", length = 255)
        private String userEdit;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

}
