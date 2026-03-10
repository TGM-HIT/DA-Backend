package at.ac.tgm.repository;

import at.ac.tgm.entity.MeldeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeldeRepository
        extends JpaRepository<MeldeEntity, Long> {
    List<MeldeEntity> findByBootstickId(Long bootstickId);
}