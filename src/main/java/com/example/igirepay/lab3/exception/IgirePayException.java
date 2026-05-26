package com.example.igirepay.lab3.exception;

public class IgirePayException extends RuntimeException {

    public IgirePayException(String message) {
        super(message);
    }

    public IgirePayException(String message, Throwable cause) {
        super(message, cause);
    }
}
