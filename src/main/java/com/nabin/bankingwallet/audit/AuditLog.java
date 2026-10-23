package com.nabin.bankingwallet.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(nullable = false,
            length = 50)
    private String action;

    @Column(name = "entity_type",
            length = 30)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(length = 4000)
    private String details;

    @Column(name = "ip_address",
            length = 45)
    private String ipAddress;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}