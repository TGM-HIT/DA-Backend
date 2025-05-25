package at.ac.tgm.repository;

import at.ac.tgm.entity.ProjektBewerbung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjektBewerbungRepository extends JpaRepository<ProjektBewerbung, Long> {

    // Optional: Suchmethoden
    List<ProjektBewerbung> findBySamAccountNameIgnoreCase(String samAccountName);

    List<ProjektBewerbung> findByProjektId(Long projektId);

    Optional<ProjektBewerbung> findByProjektIdAndSamAccountNameIgnoreCase(Long projektId, String samAccountName);

    // Sucht nach einer Bewerbung anhand Projekt-ID, SamAccountName und Priorität
    Optional<ProjektBewerbung> findByProjektIdAndSamAccountNameIgnoreCaseAndPrioritaet(Long projektId, String samAccountName, Integer prioritaet);
}
