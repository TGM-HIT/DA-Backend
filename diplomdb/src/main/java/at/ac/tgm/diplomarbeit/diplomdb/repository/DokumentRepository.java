package at.ac.tgm.diplomarbeit.diplomdb.repository;

import at.ac.tgm.diplomarbeit.diplomdb.entity.Dokument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DokumentRepository extends JpaRepository<Dokument, Long> {
}
