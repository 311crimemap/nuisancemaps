package com.quirkshop.nuisancemaps.config;

public class InvalidCoordinateException extends Exception {
    public InvalidCoordinateException(String errorMessage) {
        super(errorMessage);
    }
}
