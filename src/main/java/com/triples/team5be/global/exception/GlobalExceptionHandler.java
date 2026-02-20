package com.triples.team5be.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // IllegalArgumentException -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<SimpleErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SimpleErrorResponse(e.getMessage()));
    }

    // 400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<SimpleErrorResponse> handleBadRequest(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SimpleErrorResponse(e.getMessage()));
    }

    // 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(e.getMessage()));
    }

    // 401
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<SimpleErrorResponse> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new SimpleErrorResponse(e.getMessage()));
    }

    // path variable 타입이 잘못된 경우 예: /api/posts/abc/likes
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<SimpleErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SimpleErrorResponse("Invalid parameter type."));
    }

    // 나머지 예외: 최소한 메시지 숨기고 500 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleErrorResponse("Internal server error."));
    }
}