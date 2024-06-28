package com.quirkshop.nuisancemaps.config;

public class MissingCoordinateException extends Exception {
    public MissingCoordinateException(String errorMessage) {
        super(errorMessage);
    }
}
