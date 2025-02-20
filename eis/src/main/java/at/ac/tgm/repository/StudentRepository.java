package at.ac.tgm.repository;

import at.ac.tgm.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    // ggf. eigene Query-Methoden

    // z.B. damit wir nach studentKennzahl suchen können
    Optional<Student> findByStudentKennzahl(String studentKennzahl);
    void deleteByStudentKennzahl(String studentKennzahl);
    boolean existsByStudentKennzahl(String studentKennzahl);
}