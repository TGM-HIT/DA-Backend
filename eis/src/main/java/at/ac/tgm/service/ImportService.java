package at.ac.tgm.service;

import at.ac.tgm.model.*;
import at.ac.tgm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ImportService {

    private final TeacherRepository teacherRepository;
    private final HitclassRepository hitclassRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;

    // Eine einzige Liste aller unerwünschten Titel
    private static final List<String> KNOWN_TITLES = Arrays.asList(
            "Mag.a", "MMag", "Mag.", "MSc", "DI", "(FH)", "Dipl.-Ing.",
            "BED", "BEd", "Bed", "BSc", "Dr", "Ing", "Bakk.techn",
            "Prof", "MA", "MEd", "rer.nat.", "PhD", "FL", "FOL",
            "BA", "Dipl.-Päd.", "techn.", "Mag.rer.nat.", "Ing.Mag.", "DI(FH)", "Dr.techn.",
            "Mag.Dr.", "Ing.Dipl.-Päd.", "MMag.Dr.", "Dipl.-Ing.(FH)", "Dipl.-Ing.Dr.", "MAS", "BSc(WU)", "Mag.phil.", "Ing.DI", "Ing.Dr.", "Mag.(FH)"
    );

    @Autowired
    public ImportService(TeacherRepository teacherRepository,
                         HitclassRepository hitclassRepository,
                         StudentRepository studentRepository,
                         SubjectRepository subjectRepository,
                         LessonRepository lessonRepository) {
        this.teacherRepository = teacherRepository;
        this.hitclassRepository = hitclassRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.lessonRepository = lessonRepository;
    }

    /**
     * Methode, um ein ganzes CSV-File (MultipartFile) einzulesen.
     */
    @Transactional
    public void importCsv(MultipartFile file) throws Exception {
        System.out.println("Daten werden vearbeitet...");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = br.readLine()) != null) {
                importCsvLine(line);
            }
        }
        System.out.println("Daten erfolgreich gespeichert.");
    }

    /**
     * Verarbeitet eine einzelne Zeile aus der CSV
     */
    @Transactional
    public void importCsvLine(String csvLine) {
        String[] cols = csvLine.split(";");
        if (cols.length < 16) {
            return;
        }

        // Spalten (Index von 0 an gezählt)
        String klasse = cols[1].trim();
        String studentKennzahl = cols[4].trim();
        String nachname = cols[5].trim();
        String vorname = cols[6].trim();
        String gegenstandsart = cols[11].trim();
        String kurzbezeichnung = cols[13].trim();
        String langbezeichnung = cols[14].trim();
        String lehrerNamen = cols[15].trim();

        // Filter: nur Klassen mit "HIT"
        if (!klasse.contains("HIT")) {
            return;
        }

        // 1) Hitclass holen/erzeugen
        Hitclass hitclass = hitclassRepository.findByName(klasse)
                .orElseGet(() -> {
                    Hitclass newHitclass = Hitclass.builder().name(klasse).build();
                    return hitclassRepository.save(newHitclass);
                });

        // 2) Student holen oder anlegen
        Student student = studentRepository.findByStudentKennzahl(studentKennzahl)
                .orElseGet(() -> {
                    Student newStudent = Student.builder()
                            .vorname(vorname)
                            .nachname(nachname)
                            .studentKennzahl(studentKennzahl)
                            .hitclass(hitclass)
                            .build();
                    return studentRepository.save(newStudent);
                });

        // 3) Subject holen oder anlegen
        Subject subject = subjectRepository.findByKurzbezeichnungAndGegenstandsartAndLangbezeichnung(kurzbezeichnung, gegenstandsart, langbezeichnung)
                .orElseGet(() -> {
                    Subject newSubject = Subject.builder()
                            .kurzbezeichnung(kurzbezeichnung)
                            .gegenstandsart(gegenstandsart)
                            .langbezeichnung(langbezeichnung)
                            .build();
                    return subjectRepository.save(newSubject);
                });

        // 4) Lesson holen oder anlegen
        Lesson lesson = lessonRepository.findBySubjectIdAndHitclassId(subject.getId(), hitclass.getId())
                .orElseGet(() -> {
                    Lesson newLesson = Lesson.builder()
                            .subject(subject)
                            .hitclass(hitclass)
                            .build();
                    return lessonRepository.save(newLesson);
                });

        // 5) Lehrer verarbeiten
        String[] lehrerArray = lehrerNamen.split(",");
        Set<Teacher> teachers = new HashSet<>();
        Teacher klassenvorstand = null;

        for (String lehrerName : lehrerArray) {
            lehrerName = lehrerName.trim();
            if (!lehrerName.isEmpty()) {
                // --- Titel entfernen! ---
                lehrerName = removeTitles(lehrerName);

                // Zusätzliche Überprüfung nach dem Entfernen der Titel
                if (lehrerName.isEmpty()) {
                    continue;
                }

                // Teacher holen oder anlegen
                String finalLehrerName = lehrerName;
                Teacher teacher = teacherRepository.findByNameIgnoreCase(lehrerName)
                        .orElseGet(() -> {
                            Teacher newTeacher = Teacher.builder()
                                    .name(finalLehrerName)
                                    .build();
                            return teacherRepository.save(newTeacher);
                        });

                teachers.add(teacher);

                // Klassenvorstand bei "Allgemeines"
                if (gegenstandsart.equalsIgnoreCase("Allgemeines") && hitclass.getKlassenvorstand() == null) {
                    hitclass.setKlassenvorstand(teacher);
                }
            }
        }


        // Keine else-Klausel mehr, um Klassenvorstand nicht auf null zu setzen
        hitclassRepository.save(hitclass);

        // 6) Lehrer zu Lesson hinzufügen
        if (lesson.getTeachers() == null) {
            lesson.setTeachers(new HashSet<>());
        }
        lesson.getTeachers().addAll(teachers);
        lessonRepository.save(lesson);
    }

    /**
     * Entfernt bekannte Titel aus einem Lehrer-Namen,
     * z.B. "Mag.", "Dr.", usw., ohne Teile des eigentlichen Namens zu löschen.
     */
    private String removeTitles(String fullName) {
        // Split an Leerzeichen
        String[] tokens = fullName.split("\\s+");
        List<String> filtered = new ArrayList<>();

        for (String token : tokens) {
            // Entferne am Ende '.' oder ','
            String cleaned = token.replaceAll("[.,]+$", "");

            // Normalisieren: Punkte/Bindestriche raus, klein schreiben
            String normalized = cleaned
                    .replace(".", "")
                    .replace("-", "")
                    .toLowerCase();

            boolean isTitle = false;
            for (String title : KNOWN_TITLES) {
                String titleNormalized = title
                        .replace(".", "")
                        .replace("-", "")
                        .toLowerCase();

                if (normalized.equals(titleNormalized)) {
                    isTitle = true;
                    break;
                }
            }
            // Nur wenn es KEIN Titel ist, übernehmen wir das Token
            if (!isTitle) {
                filtered.add(token);
            }
        }

        // Tokens wieder zusammenfügen
        return String.join(" ", filtered).trim();
    }
}
