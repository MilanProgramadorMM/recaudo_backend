package com.recaudo.api.domain.model.entity;

import com.recaudo.api.domain.model.constant.CreditStatusCode;
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
@Table(name = "observation_presolicitud")
public class ObservationCreditIntentionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_intention_id")
    private Long creditIntentionId;

    @Column(name = "credit_intention_status_start")
    private String creditIntentionStatusStart;

    @Column(name = "credit_intention_status_end")
    private String creditIntentionStatusEnd;

    @Column(name = "activity")
    private String activity;

    @Column(name = "observation")
    private String observation;

    @Column(name = "user_create")
    private String userCreate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


}
