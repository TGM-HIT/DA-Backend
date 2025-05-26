package at.ac.tgm.repository;

import at.ac.tgm.entity.Betreuer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BetreuerRepository extends JpaRepository<Betreuer, Long> {
    Optional<Betreuer> findBySamAccountNameIgnoreCase(String samAccountName);
}
