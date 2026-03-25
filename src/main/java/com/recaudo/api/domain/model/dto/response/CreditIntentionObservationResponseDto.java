package com.recaudo.api.domain.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;


@Data
@Builder
@Getter
@Setter
public class CreditIntentionObservationResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("credit_intention_id")
    private Long creditIntentionId;

    @JsonProperty("credit_intention_status_start")
    private String creditIntentionStatusStart;

    @JsonProperty("credit_intention_status_end")
    private String creditIntentionStatusEnd;

    @JsonProperty("activity")
    private String activity;

    @JsonProperty("observation")
    private String observation;

    @JsonProperty("user_create")
    private String userCreate;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

}
