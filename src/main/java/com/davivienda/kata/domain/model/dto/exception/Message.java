package com.davivienda.kata.domain.model.dto.exception;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Getter
@Component
public class Message implements Serializable {

    private static final long serialVersionUID = 1652359669445648982L;

    @Value("${message.exception.general}")
    private String general;

    @Value("${message.exception.unauthorized}")
    private String unauthorized;

    @Value("${message.exception.forbidden}")
    private String forbidden;

    @Value("${message.exception.not-found}")
    private String notFound;

    @Value("${message.exception.username-not-found}")
    private String usernameNotFound;

    private String inconsistency;

    @Value("${message.exception.bad-request}")
    private String badRequest;

    @Value("${message.exception.internal-server-error}")
    private String internalServerError;

    @Value("${message.exception.bad-credentials}")
    private String badCredentials;

    private String methodNoAllow;

    private String missingServletRequestParameter;

    @Value("${message.exception.constraint-violation}")
    private String constraintViolation;

    private String argumentTypeMismatch;

    @Value("${message.exception.media-type-no-supported}")
    private String mediaTypeNoSupported;

    private String customNoData;

    private String nullField;

    private String noNullField;

    private String customExistData;

    public String getMessage(String message, Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String parameter = entry.getKey();
            String value = entry.getValue();
            message = message.replace(parameter, value);
        }
        return message;
    }

}
