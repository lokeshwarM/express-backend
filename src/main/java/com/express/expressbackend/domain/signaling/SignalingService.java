package com.express.expressbackend.domain.signaling;

import org.springframework.stereotype.Service;
import java.util.UUID;
import jakarta.transaction.Transactional;
import com.express.expressbackend.domain.signaling.SignalingResponse;

@Service
public class SignalingService {

    private final SignalingRepository repo;

    public SignalingService(SignalingRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void saveOffer(UUID sessionId, String offer) {
        SignalingMessage msg = repo.findBySessionId(sessionId)
                .orElseGet(() -> {
                    SignalingMessage m = new SignalingMessage();
                    m.setSessionId(sessionId);
                    m.setCandidates("");
                    return m;
                });

        msg.setOffer(offer);

        repo.save(msg);
    }

    @Transactional
    public void saveAnswer(UUID sessionId, String answer) {
        SignalingMessage msg = repo.findBySessionId(sessionId)
                .orElseThrow();

        msg.setAnswer(answer);

        repo.save(msg);
    }

    @Transactional
    public SignalingResponse get(UUID sessionId) {
        SignalingMessage msg = repo.findBySessionId(sessionId).orElse(null);

        if (msg == null) return new SignalingResponse(null, null, "");

        String offer = msg.getOffer();
        String answer = msg.getAnswer();
        String candidates = msg.getCandidates();

        if (candidates == null) candidates = "";

        return new SignalingResponse(offer, answer, candidates);
    }


    @Transactional
    public void addCandidate(UUID sessionId, String candidate) {
        SignalingMessage msg = repo.findBySessionId(sessionId)
                .orElseThrow(); // 🔥 MUST EXIST

        String existing = msg.getCandidates();
        if (existing == null) existing = "";

        msg.setCandidates(existing + candidate + "|||");

        repo.save(msg);
    }
}