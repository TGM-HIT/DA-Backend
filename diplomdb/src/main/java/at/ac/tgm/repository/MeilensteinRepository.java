package at.ac.tgm.repository;

import at.ac.tgm.entity.Meilenstein;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeilensteinRepository extends JpaRepository<Meilenstein, Long> {
}
