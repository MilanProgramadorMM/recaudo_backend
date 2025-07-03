package com.davivienda.kata.domain.model.dto.rest_api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class RegisterDto {

    private final static String DEFAULT_ERROR_MESSAGE = "Credenciales incorrectos";

    @Schema(description = "Username", example = "myuser", required = true)
    @NotNull(message = "El usuario es obligatorio")
    @Size(max = 355, min = 8, message = DEFAULT_ERROR_MESSAGE)
    @JsonProperty("username")
    String username;

    @Schema(description = "User password", example = "@userPassword123", required = true)
    @NotNull(message = "La contraseña es obligatoria")
    @Size(max = 355, min = 8, message = DEFAULT_ERROR_MESSAGE)
    @JsonProperty("password1")
    String password1;

    @Schema(description = "User password", example = "@userPassword123", required = true)
    @NotNull(message = "La confirmación de contraseña es obligatoria")
    @Size(max = 355, min = 8, message = DEFAULT_ERROR_MESSAGE)
    @JsonProperty("password2")
    String password2;

}
