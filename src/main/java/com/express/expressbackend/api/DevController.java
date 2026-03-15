package com.express.expressbackend.api;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import com.express.expressbackend.domain.user.UserRole;

import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/dev")
public class DevController {

    private final UserRepository userRepository;
    private final ListenerRepository listenerRepository;

    public DevController(UserRepository userRepository,
                         ListenerRepository listenerRepository) {
        this.userRepository = userRepository;
        this.listenerRepository = listenerRepository;
    }

    @PostMapping("/seed")
    public String seed() {

        User user = new User();
        user.setEmail("testuser@example.com");
        user.setPublicDisplayId("USER123");
        user.setRole(UserRole.USER);
        user.setActive(true);
        

        userRepository.save(user);

        Listener listener = new Listener();
        
        listener.setUser(user);
        listener.setAvailable(true);
        listener.setBlacklisted(false);
        listener.setRedFlagCount(0);
        listener.setCreatedAt(OffsetDateTime.now());

        listenerRepository.save(listener);

        return "User ID: " + user.getId() + "\nListener ID: " + listener.getId();
    }
}