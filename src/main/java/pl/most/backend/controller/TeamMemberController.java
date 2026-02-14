package pl.most.backend.controller;

import pl.most.backend.model.entity.TeamMember;
import pl.most.backend.service.TeamScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamScraperService teamScraperService;

    @PostMapping("/scrape")
    public ResponseEntity<List<TeamMember>> scrapeTeamMembers() {
        try {
            List<TeamMember> members = teamScraperService.scrapeAndSaveTeamMembers();
            return ResponseEntity.ok(members);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<TeamMember>> getAllMembers() {
        List<TeamMember> members = teamScraperService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamMember> getMemberById(@PathVariable Long id) {
        TeamMember member = teamScraperService.getMemberById(id);
        if (member != null) {
            return ResponseEntity.ok(member);
        }
        return ResponseEntity.notFound().build();
    }
}
