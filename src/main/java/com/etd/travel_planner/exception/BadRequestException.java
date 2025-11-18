package com.etd.travel_planner.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {

    private final String fieldName;

    public BadRequestException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

}
