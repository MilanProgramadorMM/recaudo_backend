package com.recaudo.api.domain.model.dto.rest_api;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserPasswordDto {

    @Size(min = 8, message = "valor debe contener mas de 8 caracteres")
    private String newPassword;
}
