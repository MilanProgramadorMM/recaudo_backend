package com.recaudo.api.domain.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class CreditIntentionStatusResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("credit_intention_id")
    private Long creditIntentionId;

    @JsonProperty("code")
    private String code;

    @JsonProperty("user_start")
    private String userStart;

    @JsonProperty("user_end")
    private String userEnd;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

}
