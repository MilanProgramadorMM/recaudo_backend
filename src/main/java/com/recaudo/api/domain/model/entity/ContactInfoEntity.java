package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contact_info", schema = "public")
public class ContactInfoEntity implements Serializable {

    @Id
    @SequenceGenerator(name = "contact_info_id_seq", sequenceName = "contact_info_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "contact_info_id_seq")
    private Long id;

    @Column(name = "person")
    private Long person;

    @Column(name = "type")
    private Long type;

    @Column(name = "value")
    private String value;

    @Column(name = "country")
    private Long country;

    @Column(name = "department")
    private Long department;

    @Column(name = "city")
    private Long city;

    @Column(name = "neighborhood")
    private Long neighborhood;

    @Column(name = "description")
    private String description;

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
