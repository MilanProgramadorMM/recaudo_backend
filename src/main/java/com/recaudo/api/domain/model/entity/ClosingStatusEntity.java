package com.recaudo.api.domain.model.entity;

import com.recaudo.api.domain.model.constant.ClosingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "closing_status")
public class ClosingStatusEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "closing_id")
    private Long closingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "code")
    private ClosingStatus closingStatus;

    @Builder.Default
    @Column(name = "status")
    private Boolean status = true;

    @Column(name = "user_start")
    private String userStart;

    @Column(name = "user_end")
    private String userEnd;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;


}
