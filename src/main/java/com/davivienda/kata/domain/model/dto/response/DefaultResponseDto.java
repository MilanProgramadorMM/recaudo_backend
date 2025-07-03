package com.davivienda.kata.domain.model.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
public class DefaultResponseDto<T> {

    T data;

    String message;

    String details;

    @Builder.Default
    HttpStatus status = HttpStatus.OK;

    @Builder.Default
    String timestamp = LocalDateTime.now().toString();

}