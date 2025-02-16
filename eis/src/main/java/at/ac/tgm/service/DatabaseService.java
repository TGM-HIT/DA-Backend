package at.ac.tgm.service;

import at.ac.tgm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    private final AmpelRepository ampelRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;
    // Füge weitere Repositories hinzu, falls notwendig

    @Autowired
    public DatabaseService(
            AmpelRepository ampelRepository,
            LessonRepository lessonRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            SubjectRepository subjectRepository,
            ClassroomRepository classroomRepository
            // Initialisiere weitere Repositories
    ) {
        this.ampelRepository = ampelRepository;
        this.lessonRepository = lessonRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.classroomRepository = classroomRepository;
        // Initialisiere weitere Repositories
    }

    /**
     * Löscht alle Daten aus der Datenbank.
     * Die Löschreihenfolge muss den Foreign-Key-Beschränkungen entsprechen.
     */
    @Transactional
    public void deleteAllData() {
        logger.info("Beginne mit dem Löschen aller Daten in der Datenbank.");

        try {
            // 1. Ampel löschen
            ampelRepository.deleteAll();
            logger.info("Alle Ampel-Daten gelöscht.");

            // 2. Lektion löschen
            lessonRepository.deleteAll();
            logger.info("Alle Lektion-Daten gelöscht.");

            // 3. Schüler löschen
            studentRepository.deleteAll();
            logger.info("Alle Schüler-Daten gelöscht.");

            // 4. Lehrer löschen
            teacherRepository.deleteAll();
            logger.info("Alle Lehrer-Daten gelöscht.");

            // 5. Fach löschen
            subjectRepository.deleteAll();
            logger.info("Alle Fach-Daten gelöscht.");

            // 6. Klasse löschen
            classroomRepository.deleteAll();
            logger.info("Alle Klassen-Daten gelöscht.");

            // Füge hier weitere Entitäten entsprechend hinzu
        } catch (Exception e) {
            logger.error("Fehler beim Löschen der Datenbank: ", e);
            throw e; // Löst die Transaktion zurück
        }

        logger.info("Alle Daten wurden erfolgreich gelöscht.");
    }
}
