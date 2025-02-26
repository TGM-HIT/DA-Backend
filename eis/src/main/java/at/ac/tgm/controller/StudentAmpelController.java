package at.ac.tgm.controller;

import at.ac.tgm.dto.AmpelDto;
import at.ac.tgm.dto.ErrorResponseDto;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.model.*;
import at.ac.tgm.repository.AmpelRepository;
import at.ac.tgm.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/student-ampel")
public class StudentAmpelController {

    private final AmpelRepository ampelRepository;
    private final StudentRepository studentRepository;
    @Autowired
    private UserService userService;

    public StudentAmpelController(AmpelRepository ampelRepository, StudentRepository studentRepository) {
        this.ampelRepository = ampelRepository;
        this.studentRepository = studentRepository;
    }


    @Secured(Roles.STUDENT)
    @GetMapping("/getSchueler")
    public ResponseEntity<?> getAmpelForStudent(Authentication authentication) {
        String sAMAccountName = authentication.getName();

        // 1) Prüfen, ob wir überhaupt einen gültigen Login haben
        Optional<UserEntry> userEntryOptional = userService.findBysAMAccountName(sAMAccountName);
        if (userEntryOptional.isEmpty()) {
            // Hier könnte man 401 oder 403 zurückgeben
            return ResponseEntity.status(404).body(List.of());
        }

        // 2) Mitarbeiter- / Schülerkennung holen
        UserEntry userEntry = userEntryOptional.get();
        String employeeID = userEntry.getEmployeeID();

        // 3) Student in DB suchen
        Optional<Student> studentOptional = studentRepository.findByStudentKennzahl(employeeID);
        if (studentOptional.isEmpty()) {
            // Student nicht gefunden → 404 zurückgeben
            return ResponseEntity.status(404).body(new ErrorResponseDto("User not found in local Database", 404));
        }
        Student student = studentOptional.get();
        // 4) Ampeln abfragen
        List<Ampel> ampelList = ampelRepository.findByStudentStudentKennzahl(employeeID);

        // 5) Mapping in Dtos
        List<AmpelDto> dtoList = ampelList.stream().map(ampel -> {
            Subject subject = ampel.getLesson().getSubject();
            Teacher teacher = ampel.getTeacher();
            Lesson lesson = ampel.getLesson();
            Hitclass hitclass = lesson.getHitclass();
            String hitclassName = (hitclass != null) ? hitclass.getName() : null;

            return AmpelDto.builder()
                    .ampelId(ampel.getId())
                    .studentId(student.getId())
                    .studentName(student.getVorname() + " " + student.getNachname())
                    .teacherId((teacher != null) ? teacher.getId() : null)
                    .teacherName((teacher != null) ? teacher.getName() : null)
                    .subjectKurzbezeichnung(subject.getKurzbezeichnung())
                    .subjectLangbezeichnung(subject.getLangbezeichnung())
                    .gegenstandsart(subject.getGegenstandsart())
                    .farbe((ampel.getFarbe() != null) ? ampel.getFarbe().name() : null)
                    .bemerkung(ampel.getBemerkung())
                    .updatedAt(ampel.getUpdatedAt())
                    .hitclassName(hitclassName)
                    .lessonId(lesson.getId())
                    .build();
        }).toList();

        // 6) Erfolgreiche Antwort mit 200 OK
        return ResponseEntity.ok(dtoList);
    }

}
