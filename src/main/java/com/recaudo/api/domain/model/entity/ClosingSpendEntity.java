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
@Table(name = "closing_spend")
public class ClosingSpendEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "closing_id")
    private Long closingId;

    @Column(name = "spend_type_id")
    private Long spendTypeId;

    @Transient
    private String typeSpend;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "description")
    private String description;

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "status")
    private Boolean status = true;

}
