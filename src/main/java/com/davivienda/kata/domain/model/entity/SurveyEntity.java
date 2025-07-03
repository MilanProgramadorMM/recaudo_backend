package com.davivienda.kata.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "survey", schema = "public")
public class SurveyEntity implements Serializable {

    private static final long serialVersionUID = -4768950189607025639L;

    @Id
    @SequenceGenerator(name = "survey_id_seq", sequenceName = "survey_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "survey")
    private String survey;

    @Column(name = "user_id")
    private Long userId;

}