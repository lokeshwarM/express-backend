package com.express.expressbackend.api;

import com.express.expressbackend.domain.session.CreateSessionRequest;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.session.SessionResponse;
import com.express.expressbackend.domain.session.SessionService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public SessionResponse createSession(@RequestBody CreateSessionRequest request) {
        return sessionService.createSession(
                request.getUserId(),
                request.getListenerId(),
                request.getType()
        );
    }
    @PostMapping("/{id}/start")
    public SessionResponse start(@PathVariable UUID id) {
        return sessionService.startSession(id);
    }

    @PostMapping("/{id}/end")
    public SessionResponse end(@PathVariable UUID id) {
        return sessionService.endSession(id);
    }
}