package com.express.expressbackend.domain.ledger;

import java.time.OffsetDateTime;
import java.util.UUID;

public class LedgerEntryResponse {

    private UUID id;
    private LedgerType type;
    private double amount;
    private OffsetDateTime createdAt;

    public LedgerEntryResponse(UUID id, LedgerType type, double amount, OffsetDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public LedgerType getType() { return type; }
    public double getAmount() { return amount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}