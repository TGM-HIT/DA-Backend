package at.ac.tgm.repository;

import at.ac.tgm.entity.RueckgabeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RueckgabeRepository extends JpaRepository<RueckgabeEntity, Long> {
}
