package com.express.expressbackend.domain.listener;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ListenerMatchingService {

    private final ListenerRepository listenerRepository;

    public ListenerMatchingService(ListenerRepository listenerRepository) {
        this.listenerRepository = listenerRepository;
    }

    @Transactional
    public MatchResponse findRandomListener() {

        Listener listener = listenerRepository.findRandomAvailableListener();

        if (listener == null) {
            throw new RuntimeException("No listeners available");
        }

        return new MatchResponse(listener.getId());
    }
}