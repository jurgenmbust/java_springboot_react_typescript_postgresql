package com.inventory.entity;
import jakarta.persistence.*;
import java.time.*;

/** Shares timestamps between entities without creating another table. */
@MappedSuperclass
public abstract class TimestampedEntity {
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    // JPA calls these callbacks before writing the entity.
    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(ZoneOffset.UTC); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(ZoneOffset.UTC); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
