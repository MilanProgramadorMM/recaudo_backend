package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "holidays")
public class HolidayEntity {

    @Id
    @Column(name = "holi_date")
    private LocalDate holiDate;

    @Column(name = "holi_status")
    private String holiStatus;
}