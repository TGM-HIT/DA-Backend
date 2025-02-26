package at.ac.tgm.controller;

import at.ac.tgm.dto.AmpelRequestDto;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.model.*;
import at.ac.tgm.repository.AmpelRepository;
import at.ac.tgm.repository.LessonRepository;
import at.ac.tgm.repository.StudentRepository;
import at.ac.tgm.repository.TeacherRepository;
import at.ac.tgm.service.TeacherAmpelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ampel")
public class AmpelController {

    private final AmpelRepository ampelRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherAmpelService teacherAmpelService;

    public AmpelController(AmpelRepository ampelRepository,
                           LessonRepository lessonRepository,
                           StudentRepository studentRepository,
                           TeacherRepository teacherRepository,
                           TeacherAmpelService teacherAmpelService) {
        this.ampelRepository = ampelRepository;
        this.lessonRepository = lessonRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.teacherAmpelService = teacherAmpelService;
    }

    /**
     * POST /api/ampel
     * Legt einen neuen Ampel-Eintrag an.
     */
    @Secured(Roles.TEACHER)
    @PostMapping
    public Ampel createAmpel(@RequestBody AmpelRequestDto dto) {
        // 1) Lesson laden
        Lesson lesson = lessonRepository.findById(dto.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found with id=" + dto.getLessonId()));

        // 2) Student laden
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found with id=" + dto.getStudentId()));

        // 3) Teacher laden
        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with id=" + dto.getTeacherId()));

        // 4) Neues Ampel-Objekt anlegen
        Ampel ampel = new Ampel();
        ampel.setLesson(lesson);
        ampel.setStudent(student);
        ampel.setTeacher(teacher);
        ampel.setFarbe(AmpelFarbe.valueOf(dto.getFarbe())); // "ROT", "GELB", etc.
        ampel.setBemerkung(dto.getBemerkung());
        ampel.setUpdatedAt(LocalDateTime.now());

        // 5) Speichern & zurückgeben
        return ampelRepository.save(ampel);
    }

    /**
     * PUT /api/ampel/{ampelId}
     * Aktualisiert einen vorhandenen Ampel-Eintrag.
     */

    @Secured(Roles.TEACHER)
    @PutMapping("/{ampelId}")
    public Ampel updateAmpel(@PathVariable Long ampelId, @RequestBody AmpelRequestDto dto) {
        // 1) Vorhandenes Ampel-Objekt laden
        Ampel ampel = ampelRepository.findById(ampelId)
                .orElseThrow(() -> new RuntimeException("Ampel not found with id=" + ampelId));

        // 2) Felder aktualisieren (wenn gewünscht)
        if (dto.getFarbe() != null) {
            ampel.setFarbe(AmpelFarbe.valueOf(dto.getFarbe()));
        }
        if (dto.getBemerkung() != null) {
            ampel.setBemerkung(dto.getBemerkung());
        }
        // Falls du lessonId/teacherId/studentId auch änderbar machen willst:
        if (dto.getLessonId() != null) {
            Lesson newLesson = lessonRepository.findById(dto.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found with id=" + dto.getLessonId()));
            ampel.setLesson(newLesson);
        }
        if (dto.getStudentId() != null) {
            Student newStudent = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found with id=" + dto.getStudentId()));
            ampel.setStudent(newStudent);
        }
        if (dto.getTeacherId() != null) {
            Teacher newTeacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found with id=" + dto.getTeacherId()));
            ampel.setTeacher(newTeacher);
        }

        // 3) updatedAt neu setzen
        ampel.setUpdatedAt(LocalDateTime.now());

        // 4) Speichern & zurückgeben
        return ampelRepository.save(ampel);
    }
    @Secured(Roles.TEACHER)
    @DeleteMapping
    public ResponseEntity<?> deleteAmpel(
            @RequestParam Long lessonId,
            @RequestParam Long studentId,
            @RequestParam Long teacherId
    ) {
        // Hier ggf. Authentifizierung prüfen oder Teacher-Check
        // Dann:
        teacherAmpelService.deleteAmpel(lessonId, studentId, teacherId);
        return ResponseEntity.noContent().build();
    }

}
