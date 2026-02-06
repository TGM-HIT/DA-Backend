package at.ac.tgm.repository;

import at.ac.tgm.entity.BootstickEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BootstickRepository extends JpaRepository<BootstickEntity, Long> {
}
