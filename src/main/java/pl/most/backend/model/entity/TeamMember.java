package pl.most.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", unique = true)
    private String memberId; // np. "dvteamid-316"

    @Column(name = "name")
    private String name; // np. "Duszpasterz"

    @Column(name = "full_name")
    private String fullName; // np. "ks. Mateusz Buczek SDB"

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "panel_id")
    private String panelId; // np. "dvteambox1436217626316"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // "O mnie" sekcja

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "facebook_url", length = 1000)
    private String facebookUrl;

    @Column(name = "section")
    private String section; // "Sekstet", "Przęsłowi", "Podprzęsłowi"

    @Column(name = "belongs_to")
    private String belongsTo; // Dla Podprzęsłowych - nazwa przęsła

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    public void setCategory(String categoryName) {
        this.section = categoryName;
    }

    public String getCategory() {
        return this.section;
    }
}
