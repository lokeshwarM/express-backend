package com.express.expressbackend.domain.auth;

public class SignupInitResponse {

    private String email;
    private boolean requiresVerification;

    public SignupInitResponse(String email, boolean requiresVerification) {
        this.email = email;
        this.requiresVerification = requiresVerification;
    }

    public String getEmail() { return email; }
    public boolean isRequiresVerification() { return requiresVerification; }
}