package com.express.expressbackend.domain.listener;

import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ListenerService {

    private final ListenerRepository listenerRepository;
    private final UserRepository userRepository;

    public ListenerService(ListenerRepository listenerRepository,
                           UserRepository userRepository) {
        this.listenerRepository = listenerRepository;
        this.userRepository = userRepository;
    }

    
    public Listener createListener(UUID userId) {

        listenerRepository.findByUserId(userId)
            .ifPresent(l -> {
                throw new RuntimeException("Listener already exists for this user");
            }
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listener listener = new Listener();
        listener.setUser(user);
        listener.setAvailable(true);
        listener.setBlacklisted(false);
        listener.setRedFlagCount(0);
        listener.setCreatedAt(OffsetDateTime.now());

        return listenerRepository.save(listener);
    }
   
   
    public Listener setAvailability(UUID listenerId, boolean available) {
        Listener listener = listenerRepository.findById(listenerId)
            .orElseThrow(() -> new RuntimeException("Listener not found"));

        if (listener.isBlacklisted() && available) {
            throw new RuntimeException("Blacklisted listener cannot go online");
        }
        
        listener.setAvailable(available);

        return listenerRepository.save(listener);
    }
}