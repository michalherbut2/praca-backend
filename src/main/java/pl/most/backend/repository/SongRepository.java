package pl.most.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.most.backend.model.entity.Song;

@Repository
public interface SongRepository extends JpaRepository<Song, String> {
    // Spring sam wygeneruje zapytanie SQL po nazwie metody!
    // Np. List<Song> findByCategory(String category);
}
