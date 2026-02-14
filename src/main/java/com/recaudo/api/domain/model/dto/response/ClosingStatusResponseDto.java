package com.recaudo.api.domain.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class ClosingStatusResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("closing_id")
    private Long closingId;

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

    @JsonProperty("zone")
    private Long zone;

}
