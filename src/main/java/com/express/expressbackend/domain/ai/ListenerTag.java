package com.express.expressbackend.domain.ai;

import com.express.expressbackend.domain.listener.Listener;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "listener_tags")
public class ListenerTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "listener_id")
    private Listener listener;

    @Column(nullable = false)
    private String tag;

    public UUID getId() { return id; }
    public Listener getListener() { return listener; }
    public String getTag() { return tag; }

    public void setListener(Listener listener) { this.listener = listener; }
    public void setTag(String tag) { this.tag = tag; }
}