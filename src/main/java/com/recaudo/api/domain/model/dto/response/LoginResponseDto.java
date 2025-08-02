package com.recaudo.api.domain.model.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LoginResponseDto {

    String user;
    String token;
    private String role;


}
