package com.express.expressbackend.api;

import com.express.expressbackend.domain.session.CreateSessionRequest;
import com.express.expressbackend.domain.session.SessionRepository;
import com.express.expressbackend.domain.session.SessionResponse;
import com.express.expressbackend.domain.session.SessionService;
import com.express.expressbackend.domain.session.SessionStatus;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import org.springframework.web.bind.annotation.*;

import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.common.AuthUtil;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionController(SessionService sessionService,
                              SessionRepository sessionRepository,
                              UserRepository userRepository) {
        this.sessionService = sessionService;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ApiResponse<SessionResponse> createSession(@RequestBody CreateSessionRequest request) {
        String email = AuthUtil.getCurrentUserEmail();
        return new ApiResponse<>(sessionService.createSession(email, request.getType()));
    }

    @PostMapping("/call")
    public ApiResponse<SessionResponse> initiateCall(@RequestBody CreateSessionRequest request) {
        String email = AuthUtil.getCurrentUserEmail();
        return new ApiResponse<>(sessionService.initiateCall(email, request.getType()));
    }

    @PostMapping("/{id}/start")
    public ApiResponse<SessionResponse> start(@PathVariable UUID id) {
        return new ApiResponse<>(sessionService.startSession(id));
    }

    @PostMapping("/{id}/end")
    public ApiResponse<SessionResponse> end(@PathVariable UUID id) {
        return new ApiResponse<>(sessionService.endSession(id));
    }

    @PostMapping("/{id}/heartbeat")
    public ApiResponse<String> heartbeat(@PathVariable UUID id) {
        sessionService.updateHeartbeat(id);
        return new ApiResponse<>("OK");
    }

    // ✅ Get single session by id — used for session recovery check
    @GetMapping("/{id}")
    public ApiResponse<SessionResponse> getById(@PathVariable UUID id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return new ApiResponse<>(sessionService.toResponse(session));
    }

    @GetMapping("/active")
    public ApiResponse<SessionResponse> getActive() {
        String email = AuthUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Session session = sessionRepository
                .findTopByUserIdAndStatusIn(
                        user.getId(),
                        List.of(SessionStatus.CREATED, SessionStatus.STARTED))
                .orElse(null);

        if (session == null) return new ApiResponse<>(null);
        return new ApiResponse<>(sessionService.toResponse(session));
    }

    @GetMapping("/my")
    public ApiResponse<List<SessionResponse>> getMyHistory() {
        String email = AuthUtil.getCurrentUserEmail();
        return new ApiResponse<>(sessionService.getMyHistory(email));
    }
}