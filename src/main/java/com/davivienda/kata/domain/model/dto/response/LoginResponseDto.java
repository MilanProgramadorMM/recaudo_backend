package com.davivienda.kata.domain.model.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LoginResponseDto {

    String user;
    String token;

}
