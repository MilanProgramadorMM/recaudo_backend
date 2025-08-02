package com.recaudo.api.domain.model.dto.exception;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor(staticName = "create")
public class ErrorInfo implements Serializable {

    private static final long serialVersionUID = 7074808751192877967L;

    /**
     * The HTTP status code
     */
    @NonNull
    private Integer status;
    /**
     * The error message associated with exception
     */
    @NonNull
    private String message;
    /**
     * List of constructed error messages
     */
    @NonNull
    private String details;
    /**
     * Date and time the exception occurred
     */
    private String timestamp = LocalDateTime.now().toString();
    /**
     * Curl uri requested
     */
    //@NonNull
    //private String uri;
}


