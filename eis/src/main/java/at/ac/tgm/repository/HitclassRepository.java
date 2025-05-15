package at.ac.tgm.repository;
import at.ac.tgm.model.Hitclass;
import at.ac.tgm.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HitclassRepository extends JpaRepository<Hitclass, Long> {
    Optional<Hitclass> findByName(String name);
    List<Hitclass> findAllByKlassenvorstand(Teacher teacher);
}