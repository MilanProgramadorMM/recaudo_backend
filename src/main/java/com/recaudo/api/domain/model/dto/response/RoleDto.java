package com.recaudo.api.domain.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RoleDto {

    private Long id;

    @JsonProperty("rol")
    private String name;

    @JsonProperty("description")
    private String description;
}
