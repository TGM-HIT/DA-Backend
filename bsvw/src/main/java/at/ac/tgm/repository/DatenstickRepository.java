package at.ac.tgm.repository;

import at.ac.tgm.entity.DatenstickEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatenstickRepository extends JpaRepository<DatenstickEntity, Long> {
}
