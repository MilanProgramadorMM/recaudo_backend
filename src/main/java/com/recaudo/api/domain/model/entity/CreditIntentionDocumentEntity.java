package com.recaudo.api.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Entity
@Table(name = "credit_intention_document")
public class CreditIntentionDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_intention_id")
    private Long creditIntentionId;

    @Column(name = "documentation_type_id")
    private Long documentationTypeId;

    @Column(name = "document_side", length = 10)
    private String documentSide;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
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
