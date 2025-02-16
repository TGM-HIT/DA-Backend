package at.ac.tgm.repository;
import at.ac.tgm.model.Classroom;
import at.ac.tgm.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findByName(String name);
    List<Classroom> findAllByKlassenvorstand(Teacher teacher);
}