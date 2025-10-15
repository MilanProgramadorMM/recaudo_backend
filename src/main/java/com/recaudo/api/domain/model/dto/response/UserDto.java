package com.recaudo.api.domain.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class UserDto {

    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("person_fullname")
    private String personFullName;

    @JsonProperty("rol")
    private List<RoleDto> rol;

    @JsonProperty("user_create")
    private String userCreate;

    @Builder.Default
    @JsonProperty("created_at")
    private String createdAt = LocalDateTime.now().toString();

    @JsonProperty("status")
    private boolean status;


}
