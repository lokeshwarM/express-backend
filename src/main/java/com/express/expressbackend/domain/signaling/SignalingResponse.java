package com.express.expressbackend.domain.signaling;

public class SignalingResponse {

    private String offer;
    private String answer;
    private String candidates;

    public SignalingResponse(String offer, String answer, String candidates) {
        this.offer = offer;
        this.answer = answer;
        this.candidates = candidates;
    }

    public String getOffer() { return offer; }
    public String getAnswer() { return answer; }
    public String getCandidates() { return candidates; }
}