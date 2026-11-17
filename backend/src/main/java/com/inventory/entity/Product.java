package com.inventory.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;

/** Maps a product to a database row. JPA manages entity changes. */
@Entity
@Table(name = "products")
public class Product extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 2000)
    private String description;
    @Column(nullable = false, unique = true, length = 64)
    private String sku;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer quantity;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    public Product() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
