package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "credit_rating_range")
@Data
@Getter
@Setter
public class CreditRatingRangeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "start")
    private Integer start;

    @Column(name = "end")
    private Integer end;

    @Column(name = "value")
    private String ratingValue;
}