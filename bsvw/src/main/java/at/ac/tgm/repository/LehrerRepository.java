package at.ac.tgm.repository;

import at.ac.tgm.entity.LehrerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LehrerRepository extends JpaRepository<LehrerEntity, Long> {
    Optional<LehrerEntity> findBySamAccountName(String samAccountName);
}
