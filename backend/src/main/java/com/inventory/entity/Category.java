package com.inventory.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;

/** Maps a category to a database row. JPA manages entity changes. */
@Entity
@Table(name = "categories")
public class Category extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(nullable = false, length = 1000)
    private String description;
    public Category() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
