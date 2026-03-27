package com.express.expressbackend.domain.auth;

public class GoogleAuthRequest {

    private String credential; // Google ID token
    private String role;       // USER or LISTENER

    public String getCredential() { return credential; }
    public void setCredential(String credential) { this.credential = credential; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}