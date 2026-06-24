package com.authapp.projectonauth.dtos;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
    String message,
    HttpStatus status,
    int statusCode
){
}
