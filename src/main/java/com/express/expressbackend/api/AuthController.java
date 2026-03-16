package com.express.expressbackend.api;

import com.express.expressbackend.domain.auth.AuthResponse;
import com.express.expressbackend.domain.auth.AuthService;
import com.express.expressbackend.domain.auth.LoginRequest;
import com.express.expressbackend.domain.auth.SignupRequest;
import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.user.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ApiResponse<AuthResponse> signup(@RequestBody SignupRequest request) {
        return new ApiResponse<>(authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        return new ApiResponse<>(authService.login(request));
    }
}