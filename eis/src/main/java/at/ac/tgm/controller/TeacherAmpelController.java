package at.ac.tgm.controller;

import at.ac.tgm.dto.AmpelDto;
import at.ac.tgm.dto.AmpelRequestDto;
import at.ac.tgm.dto.ErrorResponseDto;
import at.ac.tgm.dto.TeacherKVAmpelDto;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.model.*;
import at.ac.tgm.repository.AmpelRepository;
import at.ac.tgm.repository.HitclassRepository;
import at.ac.tgm.repository.TeacherRepository;
import at.ac.tgm.service.TeacherAmpelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher-ampel")
public class TeacherAmpelController {

    private final TeacherAmpelService teacherAmpelService;
    private final TeacherRepository teacherRepository;
    private final HitclassRepository hitclassRepository;
    private final AmpelRepository ampelRepository;
    @Autowired
    private UserService userService;

    public TeacherAmpelController(TeacherAmpelService teacherAmpelService, TeacherRepository teacherRepository, HitclassRepository hitclassRepository, AmpelRepository ampelRepository) {
        this.teacherAmpelService = teacherAmpelService;
        this.teacherRepository = teacherRepository;
        this.hitclassRepository = hitclassRepository;
        this.ampelRepository = ampelRepository;
    }

    @Secured(Roles.TEACHER)
    @GetMapping("/getLehrer")
    public ResponseEntity<?> getAmpelForTeacher(Authentication authentication) {
        String sAMAccountName = authentication.getName();

        if (sAMAccountName == null || sAMAccountName.isEmpty()) {
            return ResponseEntity.status(401).body(new ErrorResponseDto("Nicht authentifiziert.", 401));
        }

        Optional<UserEntry> userEntryOptional = userService.findBysAMAccountName(sAMAccountName);
        if (userEntryOptional.isEmpty()) {
            return ResponseEntity.status(404).body(new ErrorResponseDto("Lehrer nicht gefunden.", 404));
        }

        UserEntry userEntry = userEntryOptional.get();
        String ldapName = userEntry.getName();

        Optional<Teacher> teacherOptional = teacherRepository.findByName(ldapName);
        if (teacherOptional.isEmpty()) {
            return ResponseEntity.status(404).body(new ErrorResponseDto("Lehrer in der Datenbank nicht gefunden.", 404));
        }

        Teacher teacher = teacherOptional.get();
        return ResponseEntity.ok(teacherAmpelService.getAllAmpelForTeacher(teacher.getId()));
    }

    @Secured(Roles.TEACHER)
    @PostMapping
    public ResponseEntity<?> createAmpelForTeacher(@RequestBody AmpelRequestDto dto, Authentication authentication) {
        String sAMAccountName = authentication.getName();

        if (sAMAccountName == null || sAMAccountName.isEmpty()) {
            return ResponseEntity.status(401).body(new ErrorResponseDto("Nicht authentifiziert.", 401));
        }


        Optional<Teacher> teacherOptional = teacherAmpelService.getTeacherBySAMAccountName(sAMAccountName);
        if (teacherOptional.isEmpty()) {
            return ResponseEntity.status(404).body(new ErrorResponseDto("Lehrer in der Datenbank nicht gefunden.", 404));
        }

        Teacher teacher = teacherOptional.get();

        dto.setTeacherId(teacher.getId());

        try {
            AmpelDto createdAmpel = teacherAmpelService.createOrUpdateAmpel(dto);
            return ResponseEntity.status(201).body(createdAmpel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponseDto(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto("Interner Serverfehler.", 500));
        }
    }

    /**
     * Ändert einen bestehenden Ampel-Eintrag für den aktuell authentifizierten Lehrer.
     */
    @Secured(Roles.TEACHER)
    @PutMapping
    public ResponseEntity<?> updateAmpelForTeacher(@RequestBody AmpelRequestDto dto, Authentication authentication) {
        String sAMAccountName = authentication.getName();

        if (sAMAccountName == null || sAMAccountName.isEmpty()) {
            return ResponseEntity.status(401).body(new ErrorResponseDto("Nicht authentifiziert.", 401));
        }

        Optional<Teacher> teacherOptional = teacherAmpelService.getTeacherBySAMAccountName(sAMAccountName);
        if (teacherOptional.isEmpty()) {
            return ResponseEntity.status(404).body(new ErrorResponseDto("Lehrer in der Datenbank nicht gefunden.", 404));
        }

        Teacher teacher = teacherOptional.get();

        if (dto.getTeacherId() == null || !dto.getTeacherId().equals(teacher.getId())) {
            return ResponseEntity.status(403).body(new ErrorResponseDto("Zugriff auf diesen Ampel-Eintrag verweigert.", 403));
        }

        try {
            AmpelDto updatedAmpel = teacherAmpelService.createOrUpdateAmpel(dto);
            return ResponseEntity.ok(updatedAmpel); // 200 OK
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponseDto(e.getMessage(), 400)); // Bad Request
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto("Interner Serverfehler.", 500)); // Internal Server Error
        }
    }
    @Secured(Roles.TEACHER)
    @GetMapping("/kv/getStudents")
    public ResponseEntity<?> getKvStudents(Authentication authentication) {
        String sAMAccountName = authentication.getName();
        Optional<UserEntry> userEntryOptional = userService.findBysAMAccountName(sAMAccountName);
        UserEntry userEntry = userEntryOptional.get();
        String ldapName = userEntry.getName();

        Optional<Teacher> teacherOptional = teacherRepository.findByName(ldapName);
        Teacher teacher = teacherOptional.get();

        // 2) Alle Klassen, die diesen Teacher als Klassenvorstand haben
        List<Hitclass> kvHitclasses = hitclassRepository.findAllByKlassenvorstand(teacher);

        // 3) Alle Schüler sammeln
        List<TeacherKVAmpelDto> result = new ArrayList<>();
        for (Hitclass c : kvHitclasses) {
            for (Student s : c.getStudents()) {
                // 3a) Ampel-Liste für *diesen* Schüler (alle Teacher, alle Fächer)
                List<Ampel> ampelList = ampelRepository.findAllByStudentId(s.getId());

                // 3b) Erzeugen der AmpelDto-Liste
                List<AmpelDto> ampelDtos = ampelList.stream().map(a -> {
                    AmpelDto dto = new AmpelDto();
                    dto.setTeacherId(a.getTeacher().getId());
                    dto.setTeacherName(a.getTeacher().getName());
                    dto.setSubjectKurzbezeichnung(a.getLesson().getSubject().getKurzbezeichnung());
                    dto.setSubjectLangbezeichnung(a.getLesson().getSubject().getLangbezeichnung());
                    dto.setUpdatedAt(a.getUpdatedAt());
                    if (a.getFarbe() != null) {
                        dto.setFarbe(a.getFarbe().name());
                    }
                    dto.setBemerkung(a.getBemerkung());
                    return dto;
                }).collect(Collectors.toList());

                // 3c) KvStudentAmpelDto füllen
                TeacherKVAmpelDto studentDto = new TeacherKVAmpelDto();
                studentDto.setStudentId(s.getId());
                studentDto.setStudentName(s.getNachname() + " " + s.getVorname());
                studentDto.setStudentKennzahl(s.getStudentKennzahl());
                studentDto.setAmpelEntries(ampelDtos);

                result.add(studentDto);
            }
        }

        // 4) return
        return ResponseEntity.ok(result);
    }

}
