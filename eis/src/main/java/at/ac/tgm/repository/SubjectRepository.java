package at.ac.tgm.repository;

import at.ac.tgm.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByKurzbezeichnungAndGegenstandsartAndLangbezeichnung(String kurzbezeichnung, String gegenstandsart, String langbezeichnung);

    // z.B. damit wir nach (kurzbezeichnung, gegenstandsart, langbezeichnung) suchen
}
