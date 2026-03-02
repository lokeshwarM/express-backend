package com.express.expressbackend.domain.session;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.user.User;
import com.express.expressbackend.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import com.express.expressbackend.domain.session.SessionResponse;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ListenerRepository listenerRepository;

    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          ListenerRepository listenerRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.listenerRepository = listenerRepository;
    }

    public SessionResponse createSession(UUID userId, UUID listenerId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listener listener = listenerRepository.findById(listenerId)
                .orElseThrow(() -> new RuntimeException("Listener not found"));

        if (!listener.isAvailable()) {
            throw new RuntimeException("Listener not available");
        }

        Session session = new Session();
        session.setUser(user);
        session.setListener(listener);
        session.setStatus(SessionStatus.CREATED);
        session.setCreatedAt(OffsetDateTime.now());

        listener.setAvailable(false);

        listenerRepository.save(listener);
        Session saved = sessionRepository.save(session);
        return toResponse(saved);
    }
    public SessionResponse startSession(UUID sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.CREATED) {
            throw new RuntimeException("Session cannot be started");
        }

        session.setStatus(SessionStatus.STARTED);
        session.setStartedAt(Instant.now());

        Session saved = sessionRepository.save(session);
        return toResponse(saved);
    }

    public SessionResponse endSession(UUID sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != SessionStatus.STARTED) {
            throw new RuntimeException("Session cannot be ended");
        }

        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(Instant.now());

        Listener listener = session.getListener();
        listener.setAvailable(true);

        Session saved = sessionRepository.save(session);
        return toResponse(saved);
    }
    private SessionResponse toResponse(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getListener().getId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndedAt()
        );
    }
}