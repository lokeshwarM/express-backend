package com.express.expressbackend.domain.ai;

import com.express.expressbackend.domain.user.User;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user_tags")
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String tag; // e.g. "stress", "career", "relationships"

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTag() { return tag; }

    public void setUser(User user) { this.user = user; }
    public void setTag(String tag) { this.tag = tag; }
}