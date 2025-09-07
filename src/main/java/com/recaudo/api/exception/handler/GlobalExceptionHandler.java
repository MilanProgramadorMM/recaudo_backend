package com.recaudo.api.exception.handler;

import com.recaudo.api.domain.model.dto.ResponseDto;
import com.recaudo.api.domain.model.dto.exception.ErrorInfo;
import com.recaudo.api.domain.model.dto.exception.Message;
import com.recaudo.api.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Environment env;
    private final Message message;

    private static final String PACKAGE = "Toll-Pay";
    private static final String TXT_ERROR = "ERROR - { ";
    private static final String TXT_REASON = " REASON: ";
    private static final String TXT_DETAILS = ". More info: ";

    private ResponseEntity<ErrorInfo> response(ErrorInfo error, HttpStatus httpStatus) {
        return new ResponseEntity<>(error, httpStatus);
    }

    private Map<String, String> getDataFromException(Exception exception) {
        String clase = exception.getClass().getSimpleName();
        String mensaje = (exception.getMessage() != null) ? exception.getMessage() : "No se identificó un mensaje";
        String cause = (exception.getCause() != null && exception.getCause().getCause() != null)
                ? exception.getCause().getCause().getMessage()
                : "No se identificó una causa";
        String detalle = "";
        if (exception.getStackTrace() != null && exception.getStackTrace().length > 0) {
            StackTraceElement[] stackTrace = exception.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if (stackTraceElement.getClassName().contains(PACKAGE)) {
                    detalle = stackTraceElement.toString();
                    break;
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put("class", clase);
        map.put("message", mensaje);
        map.put("cause", cause);
        map.put("details", detalle);
        return map;
    }

    public String processException(Exception e) {
        String messageException = "";
        String profile = env.getProperty("spring.profiles.active");
        if ("dev".equalsIgnoreCase(profile)) {
            Map<String, String> dataException = getDataFromException(e);
            messageException = "Clase: ".concat(dataException.get("class")).concat(". Causa: ")
                    .concat(dataException.get("cause")).concat(". Mensaje: ").concat(dataException.get("message"))
                    .concat(". Detalles: ").concat(dataException.get("details"));
        }
        return messageException;
    }

    @ExceptionHandler(InactivePersonException.class)
    public ResponseEntity<?> handleInactivePerson(InactivePersonException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "message", ex.getMessage(),
                        "personId", ex.getPersonId(),
                        "status", "INACTIVE"
                )
        );
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorInfo> handleResourceNotFoundException(ResourceNotFoundException e,
                                                                                  HttpServletRequest request) {
        String messageException = e.getMessage();
        log.warn(TXT_ERROR.concat(request.getMethod()).concat(" } ").concat(request.getRequestURI()).concat(TXT_REASON)
                .concat(messageException));
        return response(
                ErrorInfo.create(HttpServletResponse.SC_NOT_FOUND, message.getNotFound(), messageException),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorInfo> handlerUsernameNotFounExeption(
            UsernameNotFoundException e,HttpServletRequest request) {
        String messageException = e.getMessage();
        log.warn(TXT_ERROR.concat(request.getMethod()).concat(" } ").concat(request.getRequestURI()).concat(TXT_REASON)
                .concat(messageException));
        return response(
                ErrorInfo.create(HttpServletResponse.SC_NOT_FOUND, message.getNotFound(), messageException),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorInfo> handleForbiddenException(ForbiddenException e,
                                                                           HttpServletRequest request) {
        String messageException = e.getMessage();
        log.warn(TXT_ERROR.concat(request.getMethod()).concat(" } ").concat(request.getRequestURI()).concat(TXT_REASON)
                .concat(messageException));
        return response(ErrorInfo.create(HttpServletResponse.SC_FORBIDDEN, message.getForbidden(), messageException),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorInfo> handleBadRequestException(BadRequestException e,
                                                                            HttpServletRequest request) {
        String messageException = e.getMessage();
        log.warn(TXT_ERROR.concat(request.getMethod()).concat(" } ").concat(request.getRequestURI()).concat(TXT_REASON)
                .concat(messageException));
        log.error("Extensión de la excepción: ", e);
        return response(ErrorInfo.create(HttpServletResponse.SC_BAD_REQUEST, message.getBadRequest(), messageException),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorInfo> handleInternalServerErrorException(InternalServerErrorException e,
                                                                            HttpServletRequest request) {
        String messageException = e.getMessage();
        log.warn(TXT_ERROR.concat(request.getMethod()).concat(" } ").concat(request.getRequestURI()).concat(TXT_REASON)
                .concat(messageException));
        return response(ErrorInfo.create(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message.getInternalServerError(),
                        messageException),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InconsistencyException.class)
    public ResponseEntity<ErrorInfo> handleInconsistencyException(InconsistencyException e,
                                                                               HttpServletRequest request) {
        String messageException = e.getMessage();
        log.warn(TXT_ERROR.concat(request.getMethod()).concat(" } ").concat(request.getRequestURI()).concat(TXT_REASON)
                .concat(messageException));
        return response(ErrorInfo.create(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message.getInconsistency(),
                messageException), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<ErrorInfo> handleConstraintViolation(ConstraintViolationException e,
                                                                            WebRequest request) {
        String messageException = processException(e);
        StringBuilder errors = new StringBuilder();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            errors.append(
                    violation.getRootBeanClass().getName().concat(" ").concat(violation.getPropertyPath().toString())
                            .concat(": ").concat(violation.getMessage()).concat(" "));
        }
        messageException = messageException.concat(TXT_DETAILS).concat(errors.toString());
        log.warn(TXT_ERROR.concat(request.getDescription(true)).concat(" } ").concat(TXT_REASON)
                .concat(messageException));
        return response(ErrorInfo.create(HttpServletResponse.SC_BAD_REQUEST, message.getConstraintViolation(),
                messageException), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ DataIntegrityViolationException.class })
    public ResponseEntity<ErrorInfo> handleDataIntegrityViolationException(
            DataIntegrityViolationException e, WebRequest request) {
        String messageException = processException(e);
        log.warn(TXT_ERROR.concat(request.getDescription(true)).concat(" } ").concat(TXT_REASON)
                .concat(messageException != null ? messageException : ""));
        return response(ErrorInfo.create(HttpServletResponse.SC_BAD_REQUEST,
                message.getConstraintViolation(),
                messageException), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ErrorInfo> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String error = "El parámetro ".concat(e.getName()).concat(" debe ser de tipo ")
                .concat((e.getRequiredType() != null ? e.getRequiredType().getName() : ""));
        String messageException = error.concat(TXT_DETAILS).concat(e.getMessage() != null ? e.getMessage() : "");
        log.warn(TXT_ERROR.concat(request.getMethod()).concat(" } ").concat(request.getRequestURI()).concat(TXT_REASON)
                .concat(messageException));
        return response(ErrorInfo.create(HttpServletResponse.SC_BAD_REQUEST, message.getArgumentTypeMismatch(), error),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public final ResponseEntity<ErrorInfo> handleAllBadCredentialsException(Exception e, HttpServletRequest request) {
        String messageException = processException(e);
        log.error(TXT_ERROR.concat(request.getMethod()).concat(" } ").concat(request.getRequestURI()).concat(TXT_REASON)
                .concat(messageException));
        log.error("Extensión de la excepción: ", e);
        return response(
                ErrorInfo.create(HttpServletResponse.SC_BAD_REQUEST, message.getBadCredentials(), messageException),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorInfo> handleAllExceptions(Exception e, HttpServletRequest request) {
        String messageException = processException(e);
        log.error(TXT_ERROR.concat(request.getMethod()).concat(" } ").concat(request.getRequestURI()).concat(TXT_REASON)
                .concat(messageException));
        log.error("Extensión de la excepción: ", e);
        return response(
                ErrorInfo.create(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message.getGeneral(), messageException),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


