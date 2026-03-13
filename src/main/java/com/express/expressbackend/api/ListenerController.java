package com.express.expressbackend.api;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/listeners")
public class ListenerController {

    private final ListenerService listenerService;

    public ListenerController(ListenerService listenerService) {
        this.listenerService = listenerService;
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
}
