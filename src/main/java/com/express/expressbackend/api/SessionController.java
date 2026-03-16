package com.express.expressbackend.api;

import com.express.expressbackend.domain.session.CreateSessionRequest;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.session.SessionResponse;
import com.express.expressbackend.domain.session.SessionService;
import org.springframework.web.bind.annotation.*;

import com.express.expressbackend.domain.common.ApiResponse;
import com.express.expressbackend.domain.common.AuthUtil;

import java.util.UUID;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ApiResponse<SessionResponse> createSession(@RequestBody CreateSessionRequest request) {
        String email = AuthUtil.getCurrentUserEmail();
        return new ApiResponse<>(sessionService.createSession(email, request.getType()));
    }
    
    @PostMapping("/{id}/start")
    public ApiResponse<SessionResponse> start(@PathVariable UUID id) {
        return new ApiResponse<>(sessionService.startSession(id));
    }

    @PostMapping("/{id}/end")
    public ApiResponse<SessionResponse> end(@PathVariable UUID id) {
        return new ApiResponse<>(sessionService.endSession(id));
    }
}