package com.express.expressbackend.domain.common;

public class ApiResponse<T> {

    private boolean success;
    private T data;

    public ApiResponse(T data) {
        this.success = true;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }
}