package pl.most.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.most.backend.repository.TeamMemberRepository;
import pl.most.backend.service.TeamScraperService;

@Component
@RequiredArgsConstructor
public class TeamDataSeeder implements CommandLineRunner {

    private final TeamScraperService teamScraperService;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public void run(String... args) {
        if (teamMemberRepository.count() == 0) {
            System.out.println("⚠️ Baza ekipy jest pusta. Uruchamiam wstępne skrapowanie...");
            try {
                teamScraperService.scrapeAndSaveTeamMembers();
            } catch (Exception e) {
                System.err.println("❌ Błąd podczas wstępnego skrapowania: " + e.getMessage());
            }
        } else {
            System.out.println("✅ Dane ekipy już istnieją. Pomijam skrapowanie na starcie.");
        }
    }
}