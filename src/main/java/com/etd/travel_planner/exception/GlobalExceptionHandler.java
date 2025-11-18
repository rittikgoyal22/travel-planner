package com.etd.travel_planner.exception;

import com.etd.travel_planner.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDTO> handleNotFoundException(NotFoundException ex) {
        ErrorDTO errorDTO = ErrorDTO.builder()
                .message(ex.getMessage())
                .fieldName(ex.getFieldName())
                .status(HttpStatus.NOT_FOUND)
                .build();
        return new ResponseEntity<>(errorDTO, errorDTO.getStatus());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> handleBadRequestException(BadRequestException ex) {
        ErrorDTO errorDTO = ErrorDTO.builder()
                .message(ex.getMessage())
                .fieldName(ex.getFieldName())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(errorDTO, errorDTO.getStatus());
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> handleInvalidDateRangeException(InvalidDateRangeException ex) {
        ErrorDTO errorDTO = ErrorDTO.builder()
                .message(ex.getMessage())
                .fieldName(ex.getFieldName())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(errorDTO, errorDTO.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> handleInvalidDateRangeException(IllegalArgumentException ex) {
        ErrorDTO errorDTO = ErrorDTO.builder()
                .message(ex.getMessage())
                .fieldName(ex.getFieldName())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(errorDTO, errorDTO.getStatus());
    }

}
