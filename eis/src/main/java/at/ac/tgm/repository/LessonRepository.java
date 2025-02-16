package at.ac.tgm.repository;

import at.ac.tgm.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Optional<Lesson> findBySubjectIdAndClassroomId(Long subjectId, Long classroomId);
    List<Lesson> findByClassroomId(Long classroomId);

}