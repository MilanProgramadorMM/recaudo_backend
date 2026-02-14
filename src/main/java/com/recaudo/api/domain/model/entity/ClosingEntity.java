package com.recaudo.api.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "closing")
public class ClosingEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "observation")
    private String observation;

    @Column(name = "amount_admin")
    private Double amountAdmin;

    @Column(name = "amount_asesor")
    private Double amountAsesor;

    @Column(name = "delivery_type")
    private String deliveryType;

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

    @Column(name = "zona_id")
    private Long zonaId;

}
