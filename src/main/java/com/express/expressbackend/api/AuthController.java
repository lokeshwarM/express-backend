package com.express.expressbackend.api;

import com.express.expressbackend.domain.auth.*;
import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.otp.OtpType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ✅ New 2-step signup
    @PostMapping("/send-signup-otp")
    public ApiResponse<String> sendSignupOtp(@RequestBody SendSignupOtpRequest request) {
        authService.sendSignupOtp(request.getEmail());
        return new ApiResponse<>("OTP sent to " + request.getEmail());
    }

    @PostMapping("/complete-signup")
    public ApiResponse<AuthResponse> completeSignup(@RequestBody CompleteSignupRequest request) {
        return new ApiResponse<>(authService.completeSignup(request));
    }

    // Legacy
    @PostMapping("/signup")
    public ApiResponse<SignupInitResponse> signup(@RequestBody SignupRequest request) {
        return new ApiResponse<>(authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        return new ApiResponse<>(authService.login(request));
    }

    @PostMapping("/verify-email")
    public ApiResponse<AuthResponse> verifyEmail(@RequestBody VerifyOtpRequest request) {
        return new ApiResponse<>(authService.verifyEmail(request));
    }

    @PostMapping("/resend-otp")
    public ApiResponse<String> resendOtp(@RequestParam String email,
                                          @RequestParam String type) {
        authService.resendOtp(email, OtpType.valueOf(type));
        return new ApiResponse<>("OTP sent");
    }

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return new ApiResponse<>("OTP sent to your email");
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return new ApiResponse<>("Password reset successful");
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> googleAuth(@RequestBody GoogleAuthRequest request) {
        return new ApiResponse<>(authService.googleAuth(request));
    }
}