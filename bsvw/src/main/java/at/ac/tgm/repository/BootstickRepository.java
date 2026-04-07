package at.ac.tgm.repository;

import at.ac.tgm.entity.BootstickEntity;
import at.ac.tgm.enums.Schulklasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BootstickRepository extends JpaRepository<BootstickEntity, Long> {

    Optional<BootstickEntity> findByNameAndNummer(Schulklasse name, int nummer);
}
