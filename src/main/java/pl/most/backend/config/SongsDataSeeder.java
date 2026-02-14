package pl.most.backend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.most.backend.model.entity.Song;
import pl.most.backend.repository.SongRepository;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SongsDataSeeder implements CommandLineRunner {

    private final SongRepository songRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        loadSongs();
    }

    private void loadSongs() {
        // 1. Sprawdź, czy baza jest pusta (żeby nie dublować przy każdym restarcie)
        if (songRepository.count() == 0) {
            try {
                // 2. Wczytaj plik z resources
                InputStream inputStream = TypeReference.class.getResourceAsStream("/data/songs.json");

                // 3. Zamień JSON na Listę Obiektów Java
                List<Song> songs = objectMapper.readValue(inputStream, new TypeReference<List<Song>>(){});

                // 4. Zapisz wszystko do bazy jednym strzałem
                songRepository.saveAll(songs);

                System.out.println("✅ SUKCES: Załadowano " + songs.size() + " pieśni do bazy!");
            } catch (Exception e) {
                System.out.println("❌ BŁĄD ładowania pieśni: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ℹ️ Pieśni już są w bazie. Pomijam ładowanie.");
        }
    }
}
