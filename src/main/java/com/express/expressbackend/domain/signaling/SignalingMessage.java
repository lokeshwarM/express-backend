package com.express.expressbackend.domain.signaling;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class SignalingMessage {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private UUID sessionId;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    private String offer;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    private String answer;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String candidates;

    // ===== GETTERS & SETTERS =====

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCandidates() { return candidates; }
    public void setCandidates(String candidates) { this.candidates = candidates; }
}