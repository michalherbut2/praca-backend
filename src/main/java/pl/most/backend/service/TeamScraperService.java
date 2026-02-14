package pl.most.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import pl.most.backend.model.entity.TeamMember;
import pl.most.backend.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamScraperService {

    private final TeamMemberRepository teamMemberRepository;
    private final ObjectMapper objectMapper;
    private static final String TARGET_URL = "https://most.salezjanie.pl/konstrukcja/";

    private static final Map<String, String> PRZESLA_MAP = new HashMap<>();
    static {
        PRZESLA_MAP.put("dvfilter42", "Przęsło duchowe");
        PRZESLA_MAP.put("dvfilter43", "Przęsło liturgiczne");
        PRZESLA_MAP.put("dvfilter44", "Przęsło formacyjne");
        PRZESLA_MAP.put("dvfilter45", "Przęsło kulturalne");
        PRZESLA_MAP.put("dvfilter46", "Przęsło medialne");
        PRZESLA_MAP.put("dvfilter47", "Przęsło gospodarcze");
        PRZESLA_MAP.put("dvfilter48", "Przęsło sportowe");
        PRZESLA_MAP.put("dvfilter49", "Przęsło „dla innych”");
        PRZESLA_MAP.put("dvfilter50", "Przęsło turystyczne");
        PRZESLA_MAP.put("dvfilter51", "Przęsło muzyczne");
    }

    @Transactional
    public List<TeamMember> scrapeAndSaveTeamMembers() throws IOException {
        log.info("Starting scraping from: {}", TARGET_URL);

        Document document = Jsoup.connect(TARGET_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();

        List<TeamMember> members = new ArrayList<>();

        // Szukaj sekcji z nagłówkami
        Elements contentSections = document.select("section > *");
        String currentSection = null;
        String currentPrzeslo = null;

        System.out.println("Total content sections found: " + contentSections.size());

        for (Element element : contentSections) {
            // Sprawdź czy to nagłówek sekcji
            Element h3 = element.selectFirst("h3");
//            if (element.tagName().matches("h[1-6]")) {
            if (h3 != null) {
//                String headerText = element.text().trim();
                String headerText = h3.text().trim();

                if (headerText.equalsIgnoreCase("Sekstet") || headerText.contains("Sekstet")) {
                    currentSection = "Sekstet";
                    currentPrzeslo = null;
                    log.info("Found section: Sekstet");
                } else if (headerText.equalsIgnoreCase("Przęsłowi") || headerText.contains("Przęsłowi")) {
                    currentSection = "Przęsłowi";
                    currentPrzeslo = null;
                    log.info("Found section: Przęsłowi");
                } else if (headerText.equalsIgnoreCase("Podprzęsłowi") || headerText.contains("Podprzęsłowi")) {
                    currentSection = "Podprzęsłowi";
                    log.info("Found section: Podprzęsłowi");
                }
//                else if (currentSection != null && currentSection.equals("Przęsłowi") && !headerText.isEmpty()) {
//                    // To jest nazwa przęsła
//                    currentPrzeslo = headerText;
//                    log.info("Found przęsło: {}", currentPrzeslo);
//                } else if (currentSection != null && currentSection.equals("Podprzęsłowi") && !headerText.isEmpty()) {
//                    // To jest nazwa przęsła dla podprzęsłowych
//                    currentPrzeslo = headerText;
//                    log.info("Found przęsło for podprzęsłowi: {}", currentPrzeslo);
//                }
            }

            // Sprawdź czy element zawiera listę członków zespołu
            Elements memberElements = element.select("li[id^=dvteamid-]");

            for (Element memberElement : memberElements) {
                try {
                    TeamMember member = parseTeamMember(memberElement, document);
                    member.setSection(currentSection);

                    // Dla podprzęsłowych zapisz do którego przęsła należą
                    if ("Podprzęsłowi".equals(currentSection) && currentPrzeslo != null) {
                        member.setBelongsTo(currentPrzeslo);
                    }

                    // Sprawdź czy członek już istnieje w bazie
                    TeamMember savedMember;
                    if (teamMemberRepository.existsByMemberId(member.getMemberId())) {
                        TeamMember existing = teamMemberRepository.findByMemberId(member.getMemberId()).orElseThrow();
                        member.setId(existing.getId());
                        member.setCreatedAt(existing.getCreatedAt());
                        savedMember = teamMemberRepository.save(member);
                        log.info("Updated member: {} ({})", member.getFullName(), member.getSection());
                    } else {
                        savedMember = teamMemberRepository.save(member);
                        log.info("Saved new member: {} ({})", member.getFullName(), member.getSection());
                    }

                    members.add(savedMember);
                } catch (Exception e) {
                    log.error("Error parsing member element", e);
                }
            }
        }

        log.info("Total members scraped: {}", members.size());
        return members;
    }

    private TeamMember parseTeamMember(Element memberElement, Document document) {
        TeamMember member = new TeamMember();

        // Pobierz ID członka z atrybutu id (np. "dvteamid-316")
        String memberId = memberElement.attr("id");
        member.setMemberId(memberId);

        // Pobierz zdjęcie
        Element imgElement = memberElement.selectFirst("img");
        if (imgElement != null) {
            member.setImageUrl(imgElement.attr("src"));
        }

        // Pobierz nazwę/tytuł (np. "Duszpasterz")
        Element nameElement = memberElement.selectFirst(".dv-member-name");
        if (nameElement != null) {
            member.setName(nameElement.text().trim());
        }

        // Pobierz pełne imię i nazwisko (np. "ks. Mateusz Buczek SDB")
        Element infoElement = memberElement.selectFirst(".dv-member-info");
        if (infoElement != null) {
            member.setFullName(infoElement.text().trim());
        }

        // Pobierz ID panelu szczegółów z linku
        Element linkElement = memberElement.selectFirst("a[id^=dvgridboxlink]");
        if (linkElement != null) {
            String href = linkElement.attr("href");
            if (href.startsWith("#")) {
                String panelId = href.substring(1);
                member.setPanelId(panelId);

                // Teraz pobierz szczegóły z panelu
                parseDetailPanel(panelId, member, document);
            }
        }

        // Pobierz przęsło (np. "Przęsło gospodarcze")

        String dataFilterClassAttr = memberElement.attr("data-filter-class");
//        System.out.println("Member: " + member.getFullName() + ", data-filter-class: " + dataFilterClassAttr);

        for (Map.Entry<String, String> entry : PRZESLA_MAP.entrySet()) {
            String filterKey = entry.getKey(); // np. "dvfilter51"

            // Czy napis ["gridall","dvfilter51"] zawiera "dvfilter51"?
            if (dataFilterClassAttr.contains(filterKey)) {
                member.setBelongsTo(entry.getValue()); // Ustawiamy nazwę, np. "Przęsło muzyczne"
                break; // Znaleźliśmy, więc kończymy szukanie dla tej osoby
            }
        }

        return member;
    }

    private void parseDetailPanel(String panelId, TeamMember member, Document document) {
        Element panel = document.selectFirst("#" + panelId);
        if (panel == null) {
            log.warn("Panel not found for id: {}", panelId);
            return;
        }

        // Pobierz opis ("O mnie")
        Elements descriptionParagraphs = panel.select(".dv-panel-inner p");
        if (!descriptionParagraphs.isEmpty()) {
            StringBuilder description = new StringBuilder();
            for (Element p : descriptionParagraphs) {
                description.append(p.text()).append("\n\n");
            }
            member.setDescription(description.toString().trim());
        }

        // Pobierz telefon
        Element phoneElement = panel.selectFirst("a[href^=tel:]");
        if (phoneElement != null) {
            String rawHref = phoneElement.attr("href");

            // 2. Usuwamy prefix "tel:"
            String rawPhone = rawHref.replace("tel:", "").trim();

            // 3. Normalizacja: Usuwamy WSZYSTKO co nie jest cyfrą ani plusem
            // To zamieni "+48 123-456" na "+48123456"
            String cleanedPhone = rawPhone.replaceAll("[^0-9+]", "");

            if (cleanedPhone.matches("^(\\+48)?\\d{9}$")){
                if (!cleanedPhone.startsWith("+48")) {
                    cleanedPhone = "+48" + cleanedPhone;
                }

                member.setPhone(cleanedPhone);
            }
        }

        // Pobierz email
        Element emailElement = panel.selectFirst("a[href^=mailto:]");
        if (emailElement != null) {
            String email = emailElement.attr("href").replace("mailto:", "").trim();
            if (!email.isEmpty()) {
                member.setEmail(email);
            }
        }

        // Pobierz Facebook URL
        Element facebookElement = panel.selectFirst("li.facebook a");
        if (facebookElement != null) {
            member.setFacebookUrl(facebookElement.attr("href"));
        }
    }

    public List<TeamMember> getAllMembers() {
        return teamMemberRepository.findAll();
    }

    public TeamMember getMemberById(Long id) {
        return teamMemberRepository.findById(id).orElse(null);
    }

    @Scheduled(cron = "0 0 4 * * MON")
    public void scheduledScraping() {
        try {
            log.info("⏰ Uruchamiam zaplanowane skrapowanie ekipy...");
            scrapeAndSaveTeamMembers();
            log.info("✅ Zaplanowane skrapowanie zakończone.");
        } catch (Exception e) {
            log.error("❌ Błąd harmonogramu skrapowania", e);
        }
    }
}
