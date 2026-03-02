package com.express.expressbackend.domain.ledger;

import com.express.expressbackend.domain.wallet.Wallet;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerType type;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() {
        return id;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public LedgerType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public void setType(LedgerType type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}