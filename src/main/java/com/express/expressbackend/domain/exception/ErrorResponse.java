package com.express.expressbackend.domain.exception;

public class ErrorResponse {

    private boolean success;
    private String error;

    public ErrorResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }
}