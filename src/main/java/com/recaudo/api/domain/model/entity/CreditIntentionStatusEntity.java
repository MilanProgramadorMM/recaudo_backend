package com.recaudo.api.domain.model.entity;

import com.recaudo.api.infrastructure.helper.util.CreditStatusCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_intention_status")
public class CreditIntentionStatusEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_intention_id")
    private Long creditIntentionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "code")
    private CreditStatusCode code;

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
