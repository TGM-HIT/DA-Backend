package at.ac.tgm.controller;

import at.ac.tgm.dto.*;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.model.Classroom;
import at.ac.tgm.model.Lesson;
import at.ac.tgm.model.Student;
import at.ac.tgm.model.Teacher;
import at.ac.tgm.repository.*;
import at.ac.tgm.service.DatabaseService;
import at.ac.tgm.service.ImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final ImportService importService;
    private final DatabaseService databaseService;
    private final ClassroomRepository classroomRepository;
    private final TeacherRepository teacherRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final AmpelRepository ampelRepository;

    public AdminRestController(ImportService importService, DatabaseService databaseService, ClassroomRepository classroomRepository, TeacherRepository teacherRepository, LessonRepository lessonRepository, StudentRepository studentRepository, AmpelRepository ampelRepository) {
        this.importService = importService;
        this.databaseService = databaseService;
        this.classroomRepository = classroomRepository;
        this.teacherRepository = teacherRepository;
        this.lessonRepository = lessonRepository;
        this.studentRepository = studentRepository;
        this.ampelRepository = ampelRepository;
    }

    /**
     * Nimmt das hochgeladene CSV-File entgegen und ruft den ImportService auf.
     */
    @Secured(Roles.SCHUELER)
    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            importService.importCsv(file);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(Map.of("message", "File uploaded and processed successfully!"));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "Failed to upload and process file", "details", e.getMessage()));

        }
    }
    @Secured(Roles.SCHUELER)
    @DeleteMapping("/deleteAll")
    public ResponseEntity<String> deleteAllData() {
        try {
            databaseService.deleteAllData();
            return ResponseEntity.ok("Alle Daten wurden erfolgreich gelöscht.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Fehler beim Löschen der Daten: " + e.getMessage());
        }
    }
    @Secured(Roles.SCHUELER)
    @PutMapping("/setKlassenvorstand")
    public ResponseEntity<?> setKlassenvorstand(@RequestParam Long classroomId, @RequestParam Long teacherId) {
        Optional<Classroom> classroomOpt = classroomRepository.findById(classroomId);
        if (classroomOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Klasse nicht gefunden.");
        }

        Optional<Teacher> teacherOpt = teacherRepository.findById(teacherId);
        if (teacherOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Lehrer nicht gefunden.");
        }

        Classroom classroom = classroomOpt.get();
        Teacher teacher = teacherOpt.get();

        classroom.setKlassenvorstand(teacher);
        classroomRepository.save(classroom);

        return ResponseEntity.ok("Klassenvorstand wurde gesetzt!");
    }

    @Secured(Roles.SCHUELER)
    @GetMapping("/classrooms/with-teachers")
    public ResponseEntity<?> getClassroomsWithTeachers() {
        List<Classroom> classrooms = classroomRepository.findAll();

        List<ClassroomWithTeacherDto> dtoList = classrooms.stream().map(classroom -> {
            // Alle Lektionen der Klasse abrufen
            List<Lesson> lessons = lessonRepository.findByClassroomId(classroom.getId());

            // Einzigartige Lehrer aus den Lektionen extrahieren
            List<TeacherDto> teacherDtos = lessons.stream()
                    .flatMap(lesson -> lesson.getTeachers().stream())
                    .distinct()
                    .map(teacher -> new TeacherDto(teacher.getId(), teacher.getName()))
                    .collect(Collectors.toList());

            // Klassenvorstand Name abrufen
            String klassenvorstandName = (classroom.getKlassenvorstand() != null)
                    ? classroom.getKlassenvorstand().getName()
                    : null;

            return new ClassroomWithTeacherDto(
                    classroom.getId(),
                    classroom.getName(),
                    klassenvorstandName,
                    teacherDtos
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @Secured(Roles.SCHUELER)
    @PostMapping("/newStudent")
    public ResponseEntity<?> createStudent(@RequestBody CreateStudentDto dto) {
        // 1) Prüfen, ob diese Kennzahl schon existiert
        boolean alreadyExists = studentRepository.existsBySchuelerkennzahl(dto.getSchuelerkennzahl());
        if (alreadyExists) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Es existiert bereits ein Schüler mit der Kennzahl: " + dto.getSchuelerkennzahl());
        }

        // 2) Classroom-ID aus dem DTO holen und Classroom suchen
        Optional<Classroom> classroomOpt = classroomRepository.findById(dto.getClassroomId());
        if (classroomOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Classroom not found with id=" + dto.getClassroomId());
        }
        Classroom classroom = classroomOpt.get();

        // 3) Neuen Student anlegen
        Student student = Student.builder()
                .vorname(dto.getVorname())
                .nachname(dto.getNachname())
                .schuelerkennzahl(dto.getSchuelerkennzahl())
                .classroom(classroom)
                .build();

        // 4) Speichern
        Student saved = studentRepository.save(student);

        // 5) Response
        return ResponseEntity.ok("Neuer Schüler (ID=" + saved.getId() + ") erfolgreich angelegt.");
    }

    @Secured(Roles.SCHUELER)
    @GetMapping("/classrooms")
    public ResponseEntity<?> getAllClassrooms() {
        List<Classroom> classrooms = classroomRepository.findAll();
        // Reduziere auf ID + Name
        List<Map<String, Object>> result = classrooms.stream().map(c -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("name", c.getName());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @Secured(Roles.SCHUELER)
    @GetMapping("/getAllTeachers")
    public ResponseEntity<List<TeacherDto>> getAllLehrer() {
        List<Teacher> teachers = teacherRepository.findAll();

        // Map das Entity in ein DTO
        List<TeacherDto> dtos = teachers.stream()
                .map(t -> new TeacherDto(t.getId(), t.getName()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @Secured(Roles.SCHUELER)
    @GetMapping("/getAllStudents")
    public ResponseEntity<?> getKVStudents() {
        List<Student> students = studentRepository.findAll();
        List<StudentDto> dtos = students.stream()
                .map(t -> new StudentDto(t.getVorname(), t.getNachname(), t.getSchuelerkennzahl(), t.getClassroom().getName(), t.getId()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Secured(Roles.SCHUELER)
    @GetMapping("/getAllLessons")
    public ResponseEntity<?> getAllLessons() {
        List<Lesson> lessons = lessonRepository.findAll();
        List<LessonsDto> dtos = lessons.stream()
                .map(t -> new LessonsDto(t.getId(), t.getSubject().getLangbezeichnung(),t.getClassroom().getName()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Secured(Roles.SCHUELER)
    @DeleteMapping("/deleteStudent/{schuelerkennzahl}")
    @Transactional
    public ResponseEntity<?> deleteStudent(@PathVariable String schuelerkennzahl) {
        studentRepository.deleteBySchuelerkennzahl(schuelerkennzahl);
        return ResponseEntity.noContent().build();
    }

    @Secured(Roles.SCHUELER)
    @DeleteMapping("/deleteTeacher/{teacherid}")
    @Transactional
    public ResponseEntity<?> deleteTeacher(@PathVariable Long teacherid) {
        Teacher teacher = teacherRepository.findById(teacherid)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Zuordnung zu Lessons entfernen
        for (Lesson lesson : teacher.getLessons()) {
            lesson.getTeachers().remove(teacher);
            lessonRepository.save(lesson);
        }
        teacher.getLessons().clear();
        teacherRepository.save(teacher);

        // Jetzt kann der Teacher gefahrlos gelöscht werden
        teacherRepository.delete(teacher);
        return ResponseEntity.noContent().build();
    }

    @Secured(Roles.SCHUELER)
    @PostMapping("/newTeacher")
    public ResponseEntity<?> createTeacher(@RequestBody CreateTeacherDto dto) {
        if (dto.getName() == null || dto.getName().isEmpty()) {
            return ResponseEntity.badRequest().body("Name is required");
        }
        boolean alreadyExists = teacherRepository.existsByName(dto.getName());
        if (alreadyExists) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Es existiert bereits ein Lehrer mit dem Namen: " + dto.getName());
        }

        Teacher teacher = Teacher.builder()
                .name(dto.getName())
                .lessons(new HashSet<>())
                .build();

        if (dto.getLessonIds() != null && !dto.getLessonIds().isEmpty()) {
            for (Long lessonId : dto.getLessonIds()) {
                Lesson lesson = lessonRepository.findById(lessonId)
                        .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));
                teacher.getLessons().add(lesson);
                lesson.getTeachers().add(teacher);
            }
        }

        Teacher saved = teacherRepository.save(teacher);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Neuer Lehrer (ID=" + saved.getId() + ") erfolgreich angelegt.");
    }

    @Secured(Roles.SCHUELER)
    @PutMapping("/updateStudent")
    public ResponseEntity<?> updateStudent(@RequestBody UpdateStudentDto dto) {
        // 1) Student laden
        Student student = studentRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        // 2) Felder aktualisieren
        student.setVorname(dto.getVorname());
        student.setNachname(dto.getNachname());
        student.setSchuelerkennzahl(dto.getSchuelerkennzahl());
        // 3) Klasse
        Classroom classroom = classroomRepository.findById(dto.getClassroomId())
                .orElseThrow(() -> new RuntimeException("Classroom not found"));
        student.setClassroom(classroom);
        // 4) Speichern
        studentRepository.save(student);

        return ResponseEntity.ok("Student erfolgreich aktualisiert.");
    }

    @Secured(Roles.SCHUELER)
    @DeleteMapping("/deleteAllAmpel")
    public ResponseEntity<?> deleteAllAmpel() {
        ampelRepository.deleteAll();
        return ResponseEntity.ok("Alle Ampel erfolgreich gelöscht.");
    }
    @Secured(Roles.SCHUELER)
    @Transactional // Wichtig: sorgt für einen Transaktionskontext
    @PutMapping("/updateTeacher")
    public ResponseEntity<?> updateTeacherLessons(@RequestBody UpdateTeacherDto dto) {
        // 1) Lehrer laden
        Optional<Teacher> optTeacher = teacherRepository.findById(dto.getId());
        if (optTeacher.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Teacher not found with id=" + dto.getId());
        }
        Teacher teacher = optTeacher.get();

        // 3) Neue Lesson-IDs aus DTO
        List<Long> newLessonIds = (dto.getLessonIds() == null)
                ? Collections.emptyList()
                : dto.getLessonIds();

        // 4) Vorhandene Lessons "leer" machen (falls du willst,
        //    dass man alle alten entfernt und nur die neue Liste setzt)
        //    Ggf. vor dem Clear die "andere Seite" entfernen:
        for (Lesson oldLesson : teacher.getLessons()) {
            oldLesson.getTeachers().remove(teacher);
            lessonRepository.save(oldLesson);
        }
        teacher.getLessons().clear();

        // 5) Neue Lessons laden und beidseitig verknüpfen
        Set<Lesson> newLessons = new HashSet<>();
        for (Long lessonId : newLessonIds) {
            Lesson l = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));
            newLessons.add(l);
        }

        // 6) teacher -> newLessons
        teacher.getLessons().addAll(newLessons);

        // 7) newLessons -> teacher
        for (Lesson l : newLessons) {
            l.getTeachers().add(teacher);
            lessonRepository.save(l);
        }

        // 8) teacher speichern
        teacherRepository.save(teacher);

        return ResponseEntity.ok("Lehrer (ID=" + teacher.getId() + ") Lessons aktualisiert.");
    }


    @Secured(Roles.SCHUELER)
    @GetMapping("/getAllTeachersWithLessons")
    public List<TeacherWithLessonsDto> getAllTeachersWithLessons() {
        List<Teacher> teacherList = teacherRepository.findAll();

        return teacherList.stream()
                .map(teacher -> {
                    // Sammle lessonIds:
                    List<Long> lessonIds = teacher.getLessons().stream()
                            .map(Lesson::getId)
                            .collect(Collectors.toList());
                    return new TeacherWithLessonsDto(
                            teacher.getId(),
                            teacher.getName(),
                            lessonIds
                    );
                })
                .collect(Collectors.toList());
    }
}