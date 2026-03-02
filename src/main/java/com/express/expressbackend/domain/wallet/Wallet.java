package com.express.expressbackend.domain.wallet;

import com.express.expressbackend.domain.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }
}