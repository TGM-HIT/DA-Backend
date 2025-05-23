package at.ac.tgm.repository;


import at.ac.tgm.model.Ampel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmpelRepository extends JpaRepository<Ampel, Long> {

    List<Ampel> findByStudentId(Long studentId);
    Optional<Ampel> findByLessonIdAndStudentIdAndTeacherId(Long lessonId, Long studentId, Long teacherId);
    List<Ampel> findByStudentStudentKennzahl(String studentKennzahl);
    List<Ampel> findByTeacherId(Long teacherId);
    Optional<Ampel> findByTeacherIdAndStudentId(Long teacherId, Long studentId);
    List<Ampel> findAllByStudentId(Long studentId);

    @Modifying
    @Transactional
    void deleteByStudentStudentKennzahl(String studentKennzahl);

}
