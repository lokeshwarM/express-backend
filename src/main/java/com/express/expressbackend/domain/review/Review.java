package com.express.expressbackend.domain.review;

import com.express.expressbackend.domain.listener.Listener;
import com.express.expressbackend.domain.session.Session;
import com.express.expressbackend.domain.user.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    private Listener listener;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Session session;

    @Column(nullable = false)
    private int rating; // 1-5

    @Column(length = 500)
    private String comment;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getId() { return id; }
    public Listener getListener() { return listener; }
    public User getUser() { return user; }
    public Session getSession() { return session; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setListener(Listener listener) { this.listener = listener; }
    public void setUser(User user) { this.user = user; }
    public void setSession(Session session) { this.session = session; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
}