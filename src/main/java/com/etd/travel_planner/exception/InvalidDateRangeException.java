package com.etd.travel_planner.exception;

import lombok.Getter;

@Getter
public class InvalidDateRangeException extends RuntimeException {

    private final String fieldName = "Date Range";

    public InvalidDateRangeException(String message) {
        super(message);
    }

}