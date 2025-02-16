package at.ac.tgm.repository;


import at.ac.tgm.model.Ampel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AmpelRepository extends JpaRepository<Ampel, Long> {

    List<Ampel> findByStudentId(Long studentId);
    Optional<Ampel> findByLessonIdAndStudentIdAndTeacherId(Long lessonId, Long studentId, Long teacherId);
    List<Ampel> findByStudentSchuelerkennzahl(String schuelerkennzahl);
    List<Ampel> findByTeacherId(Long teacherId);
    Optional<Ampel> findByTeacherIdAndStudentId(Long teacherId, Long studentId);
    List<Ampel> findAllByStudentId(Long studentId);

}
