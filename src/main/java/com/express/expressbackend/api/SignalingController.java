package com.express.expressbackend.api;

import com.express.expressbackend.domain.signaling.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/signal")
@CrossOrigin("*")
public class SignalingController {

    private final SignalingService service;

    public SignalingController(SignalingService service) {
        this.service = service;
    }


    @GetMapping("/{sessionId}")
    public SignalingResponse get(@PathVariable String sessionId) {
        return service.get(UUID.fromString(sessionId));
    }

    @PostMapping("/{sessionId}/answer")
    public void sendAnswer(@PathVariable UUID sessionId, @RequestBody String answer) {
        service.saveAnswer(sessionId, answer);
    }

    @PostMapping("/{sessionId}/candidate")
    public void addCandidate(@PathVariable UUID sessionId, @RequestBody String candidate) {
        service.addCandidate(sessionId, candidate);
    }
    @PostMapping("/{sessionId}/offer")
    public void saveOffer(@PathVariable String sessionId, @RequestBody String offer) {
        service.saveOffer(UUID.fromString(sessionId), offer);
    }
}