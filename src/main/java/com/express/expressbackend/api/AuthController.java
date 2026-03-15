package com.express.expressbackend.api;

import com.express.expressbackend.domain.auth.AuthService;
import com.express.expressbackend.domain.auth.LoginRequest;
import com.express.expressbackend.domain.auth.SignupRequest;
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
    public User signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}