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
@Table(name = "answer", schema = "public")
public class AnswerEntity implements Serializable {

    private static final long serialVersionUID = -4768950189607025639L;

    @Id
    @SequenceGenerator(name = "answer_id_seq", sequenceName = "answer_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "answer_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "survey_id")
    private Long surveyId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "data")
    private String data;

}