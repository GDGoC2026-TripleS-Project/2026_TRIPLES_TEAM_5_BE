package com.triples.team5be.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SimpleErrorResponse {
    private String message;
}