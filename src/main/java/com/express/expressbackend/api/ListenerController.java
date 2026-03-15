package com.express.expressbackend.api;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerMatchingService;
import com.express.expressbackend.domain.listener.ListenerService;
import com.express.expressbackend.domain.listener.MatchResponse;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/listeners")
public class ListenerController {

    private final ListenerService listenerService;
    private final ListenerMatchingService matchingService;

    public ListenerController(ListenerService listenerService, ListenerMatchingService matchingService) {
        this.listenerService = listenerService;
        this.matchingService = matchingService;
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

    @GetMapping("/match")
    public MatchResponse matchListener() {
        return matchingService.findRandomListener();
    }
}
