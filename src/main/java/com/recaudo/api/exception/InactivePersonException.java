package com.recaudo.api.exception;

public class InactivePersonException extends RuntimeException {
    private final Long personId;

    public InactivePersonException(String message, Long personId) {
        super(message);
        this.personId = personId;
    }

    public Long getPersonId() {
        return personId;
    }
}
