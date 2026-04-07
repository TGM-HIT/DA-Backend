package at.ac.tgm.repository;

import at.ac.tgm.entity.AusleiheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AusleiheRepository extends JpaRepository<AusleiheEntity, Long> {
}
