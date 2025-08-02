package com.recaudo.api.domain.model.dto.rest_api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.recaudo.api.domain.model.dto.response.RoleDto;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class UserDto {

    private Long id;

    @JsonProperty("username")
    private Long username;

    @JsonProperty("person_fullname")
    private String personFullName;

    @JsonProperty("rol")
    private RoleDto rol;

    @JsonProperty("user_create")
    private String userCreate;

    @Builder.Default
    @JsonProperty("created_at")
    private String createdAt = LocalDateTime.now().toString();

}
