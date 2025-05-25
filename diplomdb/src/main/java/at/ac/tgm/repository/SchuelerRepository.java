package at.ac.tgm.repository;

import at.ac.tgm.entity.Schueler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchuelerRepository extends JpaRepository<Schueler, String> {
    Optional<Schueler> findBySamAccountNameIgnoreCase(String samAccountName);
}
