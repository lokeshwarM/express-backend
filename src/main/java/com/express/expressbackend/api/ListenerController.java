package com.express.expressbackend.api;

import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.common.AuthUtil;
import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerMatchingService;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.listener.ListenerService;
import com.express.expressbackend.domain.listener.MatchResponse;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/listeners")
public class ListenerController {

    private final ListenerService listenerService;
    private final ListenerMatchingService matchingService;
    private final ListenerRepository listenerRepository;
    private final UserRepository userRepository;

    public ListenerController(ListenerService listenerService,
                               ListenerMatchingService matchingService,
                               ListenerRepository listenerRepository,
                               UserRepository userRepository) {
        this.listenerService = listenerService;
        this.matchingService = matchingService;
        this.listenerRepository = listenerRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Listener createListener(@RequestParam UUID userId) {
        return listenerService.createListener(userId);
    }

    @PostMapping("/{id}/availability")
    public Listener setAvailability(
            @PathVariable UUID id,
            @RequestParam boolean available) {
        return listenerService.setAvailability(id, available);
    }

    // Returns actual availability from DB — used on dashboard load
    @GetMapping("/me")
    public ApiResponse<Boolean> getMyAvailability() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Listener listener = listenerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Listener profile not found"));
        return new ApiResponse<>(listener.isAvailable());
    }

    // JWT-secured — listener dashboard calls this on toggle
    @PostMapping("/me/availability")
    public ApiResponse<String> setMyAvailability(@RequestParam boolean available) {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Listener listener = listenerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Listener profile not found"));

        if (listener.isBlacklisted() && available) {
            throw new RuntimeException("Blacklisted listener cannot go online");
        }

        listener.setAvailable(available);
        listenerRepository.save(listener);
        return new ApiResponse<>(available ? "AVAILABLE" : "UNAVAILABLE");
    }

    @GetMapping("/match")
    public MatchResponse matchListener() {
        return matchingService.findRandomListener();
    }
}