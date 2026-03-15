package com.express.expressbackend.domain.flag;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.listener.ListenerRepository;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.session.SessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FlagService {

    private final FlagRecordRepository flagRepository;
    private final SessionRepository sessionRepository;
    private final ListenerRepository listenerRepository;

    private static final int FLAG_THRESHOLD = 3;

    public FlagService(
            FlagRecordRepository flagRepository,
            SessionRepository sessionRepository,
            ListenerRepository listenerRepository) {

        this.flagRepository = flagRepository;
        this.sessionRepository = sessionRepository;
        this.listenerRepository = listenerRepository;
    }

    @Transactional
    public void createFlag(CreateFlagRequest request) {

        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Listener listener = session.getListener();

        FlagRecord flag = new FlagRecord();
        flag.setSession(session);
        flag.setListener(listener);
        flag.setReason(request.getReason());
        flag.setConfidenceScore(request.getConfidenceScore());

        flagRepository.save(flag);

        // increase red flag count
        listener.setRedFlagCount(listener.getRedFlagCount() + 1);

        // auto blacklist
        if (listener.getRedFlagCount() >= FLAG_THRESHOLD) {
            listener.setBlacklisted(true);
            listener.setAvailable(false);
        }

        listenerRepository.save(listener);
    }
}