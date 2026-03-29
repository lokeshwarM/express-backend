package com.express.expressbackend.domain.user;

import com.express.expressbackend.domain.auth.ChangePasswordRequest;
import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.common.AuthUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserProfileResponse createUser(@RequestBody User user) {
        return new UserProfileResponse(
            userService.createUser(user).getId(),
            userService.createUser(user).getEmail(),
            userService.createUser(user).getPublicDisplayId(),
            userService.createUser(user).getRole()
        );
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getCurrentUser() {
        String email = AuthUtil.getCurrentUserEmail();
        return new ApiResponse<>(userService.getCurrentUser(email));
    }

    // ✅ Change password endpoint
    @PostMapping("/me/change-password")
    public ApiResponse<String> changePassword(@RequestBody ChangePasswordRequest request) {
        String email = AuthUtil.getCurrentUserEmail();
        userService.changePassword(email, request);
        return new ApiResponse<>("Password changed successfully");
    }
}