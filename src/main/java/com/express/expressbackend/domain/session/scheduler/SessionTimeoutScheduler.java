package com.express.expressbackend.domain.session.scheduler;

import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.session.SessionRepository;
import com.express.expressbackend.domain.session.SessionService;
import com.express.expressbackend.domain.session.SessionStatus;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class SessionTimeoutScheduler {

    private final SessionRepository sessionRepository;
    private final SessionService sessionService;

    public SessionTimeoutScheduler(SessionRepository sessionRepository,
                                   SessionService sessionService) {
        this.sessionRepository = sessionRepository;
        this.sessionService = sessionService;
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void closeStuckSessions() {

        List<Session> sessions = sessionRepository.findAll();

        for (Session session : sessions) {

            if (session.getStatus() == SessionStatus.STARTED) {

                if (session.getStatus() == SessionStatus.STARTED) {

                    //  1. Inactivity timeout
                    Instant lastActivity = session.getLastActivityAt();

                    if (lastActivity != null) {
                        long secondsInactive =
                                Duration.between(lastActivity, Instant.now()).getSeconds();

                        if (secondsInactive > 60) {
                            sessionService.endSession(session.getId());
                            continue;
                        }
                    }

                    // 🔒 2. Hard max session timeout (backup)
                    Instant started = session.getStartedAt();
                    long minutes = Duration.between(started, Instant.now()).toMinutes();

                    if (minutes >= 60) {
                        sessionService.endSession(session.getId());
                    }
                }

            }
        }
    }
}