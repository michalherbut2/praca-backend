package pl.most.backend.repository;

import pl.most.backend.model.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByMemberId(String memberId);
    boolean existsByMemberId(String memberId);
}
