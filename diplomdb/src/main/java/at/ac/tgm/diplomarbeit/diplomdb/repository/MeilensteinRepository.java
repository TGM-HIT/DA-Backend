package at.ac.tgm.diplomarbeit.diplomdb.repository;

import at.ac.tgm.diplomarbeit.diplomdb.entity.Meilenstein;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeilensteinRepository extends JpaRepository<Meilenstein, Long> {
}
