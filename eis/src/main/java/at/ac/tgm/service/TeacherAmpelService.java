package at.ac.tgm.service;

import at.ac.tgm.dto.AmpelDto;
import at.ac.tgm.dto.AmpelRequestDto;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.model.*;
import at.ac.tgm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TeacherAmpelService {

    private final TeacherRepository teacherRepository;
    private final LessonRepository lessonRepository;
    private final AmpelRepository ampelRepository;
    private final StudentRepository studentRepository;
    @Autowired
    private final UserService userService;

    public TeacherAmpelService(TeacherRepository teacherRepository,
                               LessonRepository lessonRepository,
                               AmpelRepository ampelRepository,
                               StudentRepository studentRepository,
                               UserService userService) {
        this.teacherRepository = teacherRepository;
        this.lessonRepository = lessonRepository;
        this.ampelRepository = ampelRepository;
        this.studentRepository = studentRepository;
        this.userService = userService;
    }

    // create or update
    public AmpelDto createOrUpdateAmpel(AmpelRequestDto dto) {
        if (dto.getLessonId() == null || dto.getStudentId() == null || dto.getTeacherId() == null) {
            throw new RuntimeException("lessonId, studentId, teacherId must not be null");
        }

        Optional<Ampel> existingOpt = ampelRepository.findByLessonIdAndStudentIdAndTeacherId(
                dto.getLessonId(), dto.getStudentId(), dto.getTeacherId()
        );

        Ampel ampel;
        if (existingOpt.isPresent()) {
            // Update
            ampel = existingOpt.get();
            if (dto.getFarbe() != null) {
                ampel.setFarbe(AmpelFarbe.valueOf(dto.getFarbe()));
            }
            if (dto.getBemerkung() != null) {
                ampel.setBemerkung(dto.getBemerkung());
            }
            ampel.setUpdatedAt(LocalDateTime.now());
        } else {
            // Create
            Lesson lesson = lessonRepository.findById(dto.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));
            Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            ampel = new Ampel();
            ampel.setLesson(lesson);
            ampel.setTeacher(teacher);
            ampel.setStudent(student);
            ampel.setFarbe(AmpelFarbe.valueOf(dto.getFarbe()));
            ampel.setBemerkung(dto.getBemerkung());
            ampel.setUpdatedAt(LocalDateTime.now());
            ampel.setLesson(lesson);
        }

        Ampel saved = ampelRepository.save(ampel);
        return mapToDto(saved);
    }

    public void deleteAmpel(Long lessonId, Long studentId, Long teacherId) {
        Optional<Ampel> existingOpt = ampelRepository.findByLessonIdAndStudentIdAndTeacherId(
                lessonId, studentId, teacherId
        );
        if (existingOpt.isPresent()) {
            ampelRepository.delete(existingOpt.get());
        }
    }


    // Hilfsmethode
    private AmpelDto mapToDto(Ampel ampel) {
        Subject subj = ampel.getLesson().getSubject();
        Teacher t = ampel.getTeacher();
        Student s = ampel.getStudent();
        Classroom c = ampel.getLesson().getClassroom();
        Lesson lesson = ampel.getLesson();

        return AmpelDto.builder()
                .ampelId(ampel.getId())
                .teacherId(t.getId())
                .teacherName(t.getName())
                .studentId(s.getId())
                .studentName(s.getVorname() + " " + s.getNachname())
                .subjectKurzbezeichnung(subj.getKurzbezeichnung())
                .subjectLangbezeichnung(subj.getLangbezeichnung())
                .gegenstandsart(subj.getGegenstandsart())
                .classroomName(c.getName())
                .farbe((ampel.getFarbe() != null) ? ampel.getFarbe().name() : null)
                .bemerkung(ampel.getBemerkung())
                .updatedAt(ampel.getUpdatedAt())
                .lessonId(lesson.getId())
                .build();
    }

    public Optional<Teacher> getTeacherBySAMAccountName(String sAMAccountName) {
        Optional<UserEntry> userEntryOptional = userService.findBysAMAccountName(sAMAccountName);
        if (userEntryOptional.isEmpty()) {
            return Optional.empty();
        }
        String ldapName = userEntryOptional.get().getName();
        return teacherRepository.findByName(ldapName);
    }
    private AmpelDto mapEmptyToDto(Lesson lesson, Student student, Teacher teacher) {
        return AmpelDto.builder()
                .ampelId(null)                        // null, weil kein Ampel-Datensatz existiert
                .studentId(student.getId())
                .studentName(student.getVorname() + " " + student.getNachname())
                .teacherId(teacher.getId())
                .teacherName(teacher.getName())
                .subjectKurzbezeichnung(lesson.getSubject().getKurzbezeichnung())
                .subjectLangbezeichnung(lesson.getSubject().getLangbezeichnung())
                .gegenstandsart(lesson.getSubject().getGegenstandsart())
                .farbe("")                              // Leer oder Standard (z. B. "GRAU")
                .bemerkung("")                          // Keine Bemerkung
                .updatedAt(null)                          // noch kein Datum
                .classroomName(lesson.getClassroom().getName())
                .lessonId(lesson.getId())
                .build();
    }
    public List<AmpelDto> getAllAmpelForTeacher(Long teacherId) {
        // 1) Teacher laden
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id=" + teacherId));

        // 2) Alle Lessons laden, wo dieser Teacher beteiligt ist
        //    (Je nach Modell: teacher.getLessons() oder via lessonRepository.findAll() und filtern)
        //    Hier mal teacher.getLessons() als Beispiel:
        Set<Lesson> lessons = teacher.getLessons();

        // 3) Pro Lesson alle Students => lesson.getClassroom().getStudents()
        //    => Ampel-Eintrag suchen oder leeres DTO
        List<AmpelDto> result = new ArrayList<>();
        for (Lesson lesson : lessons) {
            // Alle Schüler in der Klasse
            Set<Student> students = lesson.getClassroom().getStudents();
            for (Student student : students) {
                // Ampel-Eintrag suchen
                Optional<Ampel> existing = ampelRepository.findByLessonIdAndStudentIdAndTeacherId(
                        lesson.getId(),
                        student.getId(),
                        teacherId
                );
                if (existing.isPresent()) {
                    // vorhandenen Ampel-Datensatz -> DTO
                    result.add(mapToDto(existing.get()));
                } else {
                    // kein Ampel-Eintrag -> leeres DTO (oder keins, je nach Wunsch)
                    // Du kannst dem Frontend einen "noch-nicht-vorhanden"-Eintrag zeigen:
                    result.add(mapEmptyToDto(lesson, student, teacher));
                }
            }
        }
        return result;
    }

}
