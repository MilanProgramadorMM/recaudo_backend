package com.recaudo.api.domain.model.dto.rest_api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class PersonRegisterDto {

    private static final String REQUIRED_MESSAGE = "Este campo es obligatorio";

    private Long id;

    @NotNull(message = REQUIRED_MESSAGE)
    @JsonProperty("document_type")
    private Long documentType;

    @NotBlank(message = REQUIRED_MESSAGE)
    @JsonProperty("document")
    private String document;

    @NotBlank(message = REQUIRED_MESSAGE)
    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("middlename")
    private String middleName;

    @NotBlank(message = REQUIRED_MESSAGE)
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("maternal_lastname")
    private String maternalLastname;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("gender")
    private Long gender;

    @JsonProperty("occupation")
    private String occupation;

    @JsonProperty("description")
    private String description;

    @JsonProperty("user_create")
    private String userCreate;

    @Builder.Default
    @JsonProperty("created_at")
    private String createdAt = LocalDateTime.now().toString();

    @JsonProperty("user_edit")
    private String userEdit;
}
