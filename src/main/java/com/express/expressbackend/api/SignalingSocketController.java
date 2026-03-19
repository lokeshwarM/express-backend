package com.express.expressbackend.api;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class SignalingSocketController {

    @MessageMapping("/signal")
    @SendTo("/topic/signal")
    public Map<String, Object> signal(Map<String, Object> message) {
        return message;
    }
}