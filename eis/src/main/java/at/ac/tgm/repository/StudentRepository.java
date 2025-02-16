package at.ac.tgm.repository;

import at.ac.tgm.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    // ggf. eigene Query-Methoden

    // z.B. damit wir nach schuelerkennzahl suchen können
    Optional<Student> findBySchuelerkennzahl(String schuelerkennzahl);
    void deleteBySchuelerkennzahl(String schuelerkennzahl);
    boolean existsBySchuelerkennzahl(String schuelerkennzahl);
}