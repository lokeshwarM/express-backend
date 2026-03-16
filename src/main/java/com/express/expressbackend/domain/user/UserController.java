package com.express.expressbackend.domain.user;

import org.springframework.web.bind.annotation.*;

import com.express.expressbackend.domain.common.AuthUtil;

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
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser() {

        String email = AuthUtil.getCurrentUserEmail();

        return userService.getCurrentUser(email);
    }
}