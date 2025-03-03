package at.ac.tgm.diplomarbeit.diplomdb.repository;

import at.ac.tgm.diplomarbeit.diplomdb.entity.Betreuer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BetreuerRepository extends JpaRepository<Betreuer, Long> {
    Optional<Betreuer> findBySamAccountNameIgnoreCase(String samAccountName);
}
