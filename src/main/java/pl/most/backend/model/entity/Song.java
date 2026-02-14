package pl.most.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "songs")
@Data // Generuje gettery, settery, toString (z Lomboka)
@NoArgsConstructor
@AllArgsConstructor
public class Song {

    @Id
//    @GeneratedValue(strategy = GenerationType.UUID) // Automatycznie generuje ID
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT", nullable = false) // Ważne dla długich tekstów!
    private String content;

    // UWAGA: Nie dodajemy tutaj pola 'isFavorite'!
    // Baza danych nie wie, czy Ty to lubisz, czy Twój kolega.
    // To pole dodamy dopiero na Frontendzie.

    // --- TO JEST TA MAGIA ---
    @PrePersist
    public void ensureId() {
        // Jeśli ID jest nullem (nowa piosenka), wygeneruj je.
        // Jeśli ID już jest (import z JSON), zostaw je w spokoju.
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
