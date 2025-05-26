package at.ac.tgm.repository;

import at.ac.tgm.entity.Dokument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DokumentRepository extends JpaRepository<Dokument, Long> {
}
