package pl.most.backend.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fridge_items")
@Data
@NoArgsConstructor
public class FridgeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category = Category.INNE;

    @Column(name = "added_by", nullable = false)
    private UUID addedBy;

    @Column(name = "added_by_name", nullable = false, length = 100)
    private String addedByName;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_opened", nullable = false)
    private Boolean isOpened = false;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Category {
        MLECZNE, WEDLINY, WARZYWA, OWOCE, NAPOJE, INNE
    }
}