package com.recaudo.api.exception;

public class CreditSimulationException extends RuntimeException {

    public CreditSimulationException(String message) {
        super(message);
    }

    public CreditSimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
