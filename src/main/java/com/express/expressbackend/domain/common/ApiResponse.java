package com.express.expressbackend.domain.common;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;

    // Success response
    public ApiResponse(T data) {
        this.success = true;
        this.data = data;
        this.message = null;
    }

    // Error response
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.data = null;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getMessage() { return message; }
}