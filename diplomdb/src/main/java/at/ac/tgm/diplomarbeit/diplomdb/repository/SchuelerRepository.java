package at.ac.tgm.diplomarbeit.diplomdb.repository;

import at.ac.tgm.diplomarbeit.diplomdb.entity.Schueler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchuelerRepository extends JpaRepository<Schueler, String> {
    Optional<Schueler> findBySamAccountNameIgnoreCase(String samAccountName);
}
