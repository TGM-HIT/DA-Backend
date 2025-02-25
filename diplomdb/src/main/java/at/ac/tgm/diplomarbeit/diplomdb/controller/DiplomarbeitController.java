package at.ac.tgm.diplomarbeit.diplomdb.controller;

import at.ac.tgm.diplomarbeit.diplomdb.dto.DiplomarbeitResponseDTO;
import at.ac.tgm.diplomarbeit.diplomdb.entity.Betreuer;
import at.ac.tgm.diplomarbeit.diplomdb.entity.Diplomarbeit;
import at.ac.tgm.diplomarbeit.diplomdb.entity.Dokument;
import at.ac.tgm.diplomarbeit.diplomdb.exception.ResourceNotFoundException;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.diplomarbeit.diplomdb.repository.BetreuerRepository;
import at.ac.tgm.diplomarbeit.diplomdb.repository.DiplomarbeitRepository;
import at.ac.tgm.diplomarbeit.diplomdb.repository.DokumentRepository;
import at.ac.tgm.diplomarbeit.diplomdb.repository.MeilensteinRepository;
import at.ac.tgm.diplomarbeit.diplomdb.service.BetreuerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller zur Verwaltung von Diplomarbeitsprojekten.
 *
 * Dieser Controller stellt Endpunkte für die Erstellung, Aktualisierung, Löschung und Abfrage von
 * Projekten (Diplomarbeiten) bereit. Zusätzlich werden Funktionen zur Verwaltung von zugehörigen
 * Dokumenten (Lastenheft) und der Lehrerzuweisung angeboten.
 */
@RestController
@RequestMapping("api/projects")
public class DiplomarbeitController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiplomarbeitController.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("at.ac.tgm.diplomarbeit.diplomdb.audit");

    @Autowired
    private DiplomarbeitRepository diplomarbeitRepository;

    @Autowired
    private DokumentRepository dokumentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MeilensteinRepository meilensteinRepository;

    @Autowired
    private BetreuerRepository betreuerRepository;

    // Wird benötigt, um einen Lehrer bei Bedarf automatisch (auch via LDAP) anzulegen
    @Autowired
    private BetreuerService betreuerService;

    /**
     * Erstellt ein neues Projekt (Diplomarbeit) inklusive Lastenheft-Datei.
     *
     * HTTP-Methode: POST
     * URL: /api/projects/create-with-lastenheft
     */
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    @PostMapping("/create-with-lastenheft")
    public ResponseEntity<DiplomarbeitResponseDTO> createProjectWithLastenheft(
            @RequestParam("titel") String titel,
            @RequestParam("beschreibung") String beschreibung,
            @RequestParam("startDatum") String startDatum,
            @RequestParam("endDatum") String endDatum,
            @RequestParam(value = "mitglieder", required = false) List<String> mitglieder,
            @RequestParam("lastenheft") MultipartFile lastenheftFile
    ) {
        if (titel == null || titel.trim().isEmpty()) {
            throw new IllegalArgumentException("Titel darf nicht leer sein!");
        }
        if (beschreibung == null) {
            beschreibung = "";
        }
        LocalDate start = LocalDate.parse(startDatum);
        LocalDate end = LocalDate.parse(endDatum);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Enddatum darf nicht vor dem Startdatum liegen!");
        }
        if (start.isBefore(LocalDate.now().minusYears(1))) {
            throw new IllegalArgumentException("Startdatum darf nicht mehr als ein Jahr in der Vergangenheit liegen!");
        }
        if (lastenheftFile == null || lastenheftFile.isEmpty()) {
            throw new IllegalArgumentException("Lastenheft-Datei fehlt!");
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isStudent = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        if (isStudent) {
            long existingCount = diplomarbeitRepository.countByMitarbeiterSamAccountName(currentUser);
            if (existingCount > 0) {
                throw new IllegalStateException("Ein Schüler darf nur ein Projekt erstellen.");
            }
            if (mitglieder == null) {
                mitglieder = new ArrayList<>();
            }
            if (!mitglieder.contains(currentUser)) {
                mitglieder.add(currentUser);
            }
        }

        LOGGER.info("Erstelle neues Projekt mit Lastenheft: titel={}, anzahlMitglieder={}", titel, (mitglieder != null ? mitglieder.size() : 0));

        Diplomarbeit diplom = new Diplomarbeit();
        diplom.setTitel(titel);
        diplom.setBeschreibung(beschreibung);
        diplom.setStartdatum(start);
        diplom.setEnddatum(end);
        diplom.setStatus("EINGEREICHT");
        diplom.setMitarbeiterSamAccountNames(mitglieder != null ? new HashSet<>(mitglieder) : new HashSet<>());

        // Hinweis: Bei der Selbstzuweisung über diesen Endpunkt erfolgt keine Lehrerzuordnung,
        // daher wird hier kein Betreuer-Count aktualisiert.

        Diplomarbeit savedDiplom = diplomarbeitRepository.save(diplom);

        Dokument dok = new Dokument();
        dok.setTitel("Lastenheft: " + titel);
        dok.setBeschreibung("Das Lastenheft für das Projekt: " + titel);
        dok.setTyp("Lastenheft");
        dok.setHochladungsdatum(LocalDate.now());
        dok.setDatum(LocalDate.now());
        dok.setErstellerSamAccountName(currentUser);
        dok.setDiplomarbeit(savedDiplom);

        String uniqueFileName = System.currentTimeMillis() + "_" + lastenheftFile.getOriginalFilename();
        Path filePath = Paths.get("uploads/", uniqueFileName);
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, lastenheftFile.getBytes());
        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern des Lastenhefts", e);
            throw new RuntimeException("Fehler beim Speichern des Lastenhefts.", e);
        }
        dok.setDateipfad(filePath.toString());
        dok.setDateiname(lastenheftFile.getOriginalFilename());

        dokumentRepository.save(dok);

        DiplomarbeitResponseDTO responseDTO = mapToResponseDTO(savedDiplom);
        AUDIT_LOGGER.info("Neues Projekt angelegt: ID={}, Titel={}, erstellt von User={}", savedDiplom.getProjektId(), titel, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    /**
     * Ermöglicht einem Lehrer, sich selbst als Betreuer eines Projekts anhand der Projekt-ID zuzuweisen.
     *
     * HTTP-Methode: PUT
     * URL: /api/projects/assign-teacher-by-id/self
     */
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @PutMapping("/assign-teacher-by-id/self")
    public ResponseEntity<DiplomarbeitResponseDTO> assignSelfTeacherToProjectById(
            @RequestParam Long projectId
    ) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        Diplomarbeit projekt = diplomarbeitRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projekt nicht gefunden mit ID: " + projectId));

        // Falls bereits ein Lehrer zugewiesen wurde, wird ein Fehler zurückgegeben
        if (projekt.getBetreuerSamAccountName() != null && !projekt.getBetreuerSamAccountName().isEmpty()) {
            throw new IllegalStateException("Für dieses Projekt wurde bereits ein Lehrer zugewiesen. Eine erneute Zuweisung ist nicht möglich.");
        }

        // Falls der Lehrer noch nicht existiert, wird er automatisch aus LDAP angelegt.
        Betreuer teacher = betreuerRepository.findBySamAccountNameIgnoreCase(currentUser)
                .orElseGet(() -> betreuerService.findeOderErstelleBetreuer(currentUser));

        if (teacher.getMaxProjekte() == null) {
            throw new IllegalArgumentException("Betreuer hat keine Projektkapazität angegeben.");
        }
        if (teacher.getVergebeneProjekte() == null) {
            teacher.setVergebeneProjekte(0);
        }
        if (teacher.getVergebeneProjekte() >= teacher.getMaxProjekte()) {
            throw new IllegalStateException("Der Lehrer hat keine freien Kapazitäten.");
        }
        teacher.setVergebeneProjekte(teacher.getVergebeneProjekte() + 1);
        betreuerRepository.save(teacher);

        projekt.setBetreuerSamAccountName(currentUser);
        Diplomarbeit saved = diplomarbeitRepository.save(projekt);
        AUDIT_LOGGER.info("Projekt mit ID '{}' wurde dem Lehrer '{}' zur Selbstzuweisung zugewiesen.", projectId, currentUser);
        return ResponseEntity.ok(mapToResponseDTO(saved));
    }

    /**
     * Ermöglicht einem Administrator, einem Projekt anhand der Projekt-ID einen spezifischen Lehrer zuzuweisen.
     *
     * HTTP-Methode: PUT
     * URL: /api/projects/assign-teacher-by-id/admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/assign-teacher-by-id/admin")
    public ResponseEntity<DiplomarbeitResponseDTO> assignTeacherToProjectByIdAdmin(
            @RequestParam Long projectId,
            @RequestParam String teacherSamAccountName
    ) {
        Diplomarbeit projekt = diplomarbeitRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projekt nicht gefunden mit ID: " + projectId));

        // Falls bereits ein Lehrer zugewiesen wurde, wird ein Fehler zurückgegeben
        if (projekt.getBetreuerSamAccountName() != null && !projekt.getBetreuerSamAccountName().isEmpty()) {
            throw new IllegalStateException("Für dieses Projekt wurde bereits ein Lehrer zugewiesen. Eine erneute Zuweisung ist nicht möglich.");
        }

        // Falls der angegebene Lehrer noch nicht in der lokalen Datenbank existiert,
        // wird in LDAP nach ihm gesucht und er wird automatisch angelegt.
        Betreuer teacher = betreuerRepository.findBySamAccountNameIgnoreCase(teacherSamAccountName)
                .orElseGet(() -> betreuerService.findeOderErstelleBetreuer(teacherSamAccountName));

        if (teacher.getMaxProjekte() == null) {
            throw new IllegalArgumentException("Betreuer hat keine Projektkapazität angegeben.");
        }
        if (teacher.getVergebeneProjekte() == null) {
            teacher.setVergebeneProjekte(0);
        }
        if (teacher.getVergebeneProjekte() >= teacher.getMaxProjekte()) {
            throw new IllegalStateException("Der Lehrer hat keine freien Kapazitäten.");
        }
        teacher.setVergebeneProjekte(teacher.getVergebeneProjekte() + 1);
        betreuerRepository.save(teacher);

        projekt.setBetreuerSamAccountName(teacherSamAccountName);
        Diplomarbeit saved = diplomarbeitRepository.save(projekt);
        AUDIT_LOGGER.info("Projekt mit ID '{}' wurde durch einen Administrator dem Lehrer '{}' zugewiesen.", projectId, teacherSamAccountName);
        return ResponseEntity.ok(mapToResponseDTO(saved));
    }

    /**
     * Führt eine Überprüfung (Review) eines Projekts durch, um den Status auf ANGENOMMEN oder ABGELEHNT zu setzen.
     *
     * HTTP-Methode: PUT
     * URL: /api/projects/{id}/review
     */
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @PutMapping("/{id}/review")
    public ResponseEntity<DiplomarbeitResponseDTO> reviewProject(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam(required = false) String reason
    ) {
        LOGGER.info("Überprüfung des Projekts mit ID={} mit Entscheidung: {}", id, decision);
        Diplomarbeit projekt = diplomarbeitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projekt nicht gefunden mit ID " + id));

        if ("ACCEPTED".equalsIgnoreCase(decision)) {
            projekt.setStatus("ANGENOMMEN");
            projekt.setAblehnungsgrund(null);
        } else if ("REJECTED".equalsIgnoreCase(decision)) {
            projekt.setStatus("ABGELEHNT");
            projekt.setAblehnungsgrund(reason);
        } else {
            throw new IllegalArgumentException("Ungültige Entscheidung: nur 'ACCEPTED' oder 'REJECTED' sind zulässig.");
        }

        Diplomarbeit updated = diplomarbeitRepository.save(projekt);
        AUDIT_LOGGER.info("Projekt-Review durchgeführt: ID={}, neuer Status={}", id, projekt.getStatus());
        return ResponseEntity.ok(mapToResponseDTO(updated));
    }

    /**
     * Erstellt ein neues Projekt ohne direkte Lehrerzuweisung.
     *
     * HTTP-Methode: POST
     * URL: /api/projects
     */
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    @PostMapping
    public ResponseEntity<DiplomarbeitResponseDTO> createProject(@RequestBody at.ac.tgm.diplomarbeit.diplomdb.dto.DiplomarbeitDTO diplomarbeitDTO) {
        LOGGER.info("Erstelle Projekt: {}", diplomarbeitDTO.getTitel());

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isStudent = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        if (isStudent) {
            long existingCount = diplomarbeitRepository.countByMitarbeiterSamAccountName(currentUser);
            if (existingCount > 0) {
                throw new IllegalStateException("Ein Schüler darf nur ein Projekt erstellen.");
            }
            if (diplomarbeitDTO.getMitarbeiterSamAccountNames() == null) {
                diplomarbeitDTO.setMitarbeiterSamAccountNames(new HashSet<>());
            }
            if (!diplomarbeitDTO.getMitarbeiterSamAccountNames().contains(currentUser)) {
                diplomarbeitDTO.getMitarbeiterSamAccountNames().add(currentUser);
            }
        }

        if (diplomarbeitDTO.getTitel() == null || diplomarbeitDTO.getTitel().trim().isEmpty()) {
            throw new IllegalArgumentException("Titel darf nicht leer sein!");
        }
        if(diplomarbeitDTO.getStartdatum() == null) {
            throw new IllegalArgumentException("Startdatum darf nicht null sein!");
        }
        if(diplomarbeitDTO.getStartdatum().isBefore(LocalDate.now().minusYears(1))) {
            throw new IllegalArgumentException("Startdatum darf nicht mehr als ein Jahr in der Vergangenheit liegen!");
        }
        String status = (diplomarbeitDTO.getStatus() == null || diplomarbeitDTO.getStatus().trim().isEmpty())
                ? "EINGEREICHT"
                : diplomarbeitDTO.getStatus();

        Diplomarbeit diplomarbeit = new Diplomarbeit();
        diplomarbeit.setTitel(diplomarbeitDTO.getTitel());
        diplomarbeit.setBeschreibung(diplomarbeitDTO.getBeschreibung());
        diplomarbeit.setStatus(status);
        diplomarbeit.setStartdatum(diplomarbeitDTO.getStartdatum());
        diplomarbeit.setEnddatum(diplomarbeitDTO.getEnddatum());

        // Wenn ein Betreuer (Lehrer) angegeben wurde, prüfen wir dessen Existenz im LDAP
        // und aktualisieren gleichzeitig die vergebene Projekte-Kapazität.
        if (diplomarbeitDTO.getBetreuerSamAccountName() != null && !diplomarbeitDTO.getBetreuerSamAccountName().isEmpty()) {
            if (!userExistsInLdap(diplomarbeitDTO.getBetreuerSamAccountName())) {
                throw new ResourceNotFoundException("Betreuer nicht gefunden mit sAMAccountName " + diplomarbeitDTO.getBetreuerSamAccountName());
            }
            Betreuer teacher = betreuerRepository.findBySamAccountNameIgnoreCase(diplomarbeitDTO.getBetreuerSamAccountName())
                    .orElseGet(() -> betreuerService.findeOderErstelleBetreuer(diplomarbeitDTO.getBetreuerSamAccountName()));
            if (teacher.getVergebeneProjekte() == null) {
                teacher.setVergebeneProjekte(0);
            }
            if (teacher.getVergebeneProjekte() >= teacher.getMaxProjekte()) {
                throw new IllegalStateException("Der Lehrer hat keine freien Kapazitäten.");
            }
            teacher.setVergebeneProjekte(teacher.getVergebeneProjekte() + 1);
            betreuerRepository.save(teacher);
            diplomarbeit.setBetreuerSamAccountName(diplomarbeitDTO.getBetreuerSamAccountName());
        }

        if (diplomarbeitDTO.getMitarbeiterSamAccountNames() != null && !diplomarbeitDTO.getMitarbeiterSamAccountNames().isEmpty()) {
            for (String mitarbSam : diplomarbeitDTO.getMitarbeiterSamAccountNames()) {
                if (!userExistsInLdap(mitarbSam)) {
                    throw new ResourceNotFoundException("Mitarbeiter nicht gefunden mit sAMAccountName " + mitarbSam);
                }
            }
            diplomarbeit.setMitarbeiterSamAccountNames(diplomarbeitDTO.getMitarbeiterSamAccountNames());
        }

        Diplomarbeit savedDiplomarbeit = diplomarbeitRepository.save(diplomarbeit);
        AUDIT_LOGGER.info("Projekt erstellt: ID={}, Titel={}", savedDiplomarbeit.getProjektId(), diplomarbeitDTO.getTitel());
        return ResponseEntity.status(201).body(mapToResponseDTO(savedDiplomarbeit));
    }

    /**
     * Ruft alle Projekte ab, optional gefiltert nach Suchbegriff, Datumsbereich, Status und sortiert.
     *
     * HTTP-Methode: GET
     * URL: /api/projects
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<List<at.ac.tgm.diplomarbeit.diplomdb.dto.DiplomarbeitResponseDTO>> getAllProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String vonDatum,
            @RequestParam(required = false) String bisDatum,
            @RequestParam(required = false, defaultValue = "projektId") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String status
    ) {
        LOGGER.debug("GET /projects => search={}, vonDatum={}, bisDatum={}, sortField={}, sortDirection={}, status={}",
                search, vonDatum, bisDatum, sortField, sortDirection, status);

        Set<String> allowedSortFields = Set.of("projektId", "titel", "beschreibung", "status", "startdatum", "enddatum", "betreuerSamAccountName");
        if (!allowedSortFields.contains(sortField)) {
            sortField = "projektId";
        }

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        List<Diplomarbeit> diplomarbeiten = diplomarbeitRepository.findAll(sort);

        if (vonDatum != null && !vonDatum.isEmpty() && bisDatum != null && !bisDatum.isEmpty()) {
            try {
                LocalDate von = LocalDate.parse(vonDatum);
                LocalDate bis = LocalDate.parse(bisDatum);
                diplomarbeiten = diplomarbeiten.stream()
                        .filter(d -> d.getStartdatum() != null &&
                                !d.getStartdatum().isBefore(von) &&
                                !d.getStartdatum().isAfter(bis))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                LOGGER.error("Ungültige Datumsangaben in GET /projects: vonDatum={} bisDatum={}", vonDatum, bisDatum, e);
            }
        }

        if (status != null && !status.isEmpty()) {
            String lowerStatus = status.toLowerCase();
            diplomarbeiten = diplomarbeiten.stream()
                    .filter(d -> d.getStatus() != null && d.getStatus().toLowerCase().equals(lowerStatus))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isEmpty()) {
            String lowerCaseSearch = search.toLowerCase();
            diplomarbeiten = diplomarbeiten.stream()
                    .filter(d -> (d.getTitel() != null && d.getTitel().toLowerCase().contains(lowerCaseSearch))
                            || (d.getBeschreibung() != null && d.getBeschreibung().toLowerCase().contains(lowerCaseSearch))
                            || (d.getBetreuerSamAccountName() != null && d.getBetreuerSamAccountName().toLowerCase().contains(lowerCaseSearch))
                            || (d.getMitarbeiterSamAccountNames() != null && d.getMitarbeiterSamAccountNames()
                            .stream().anyMatch(m -> m.toLowerCase().contains(lowerCaseSearch))))
                    .collect(Collectors.toList());
        }

        List<at.ac.tgm.diplomarbeit.diplomdb.dto.DiplomarbeitResponseDTO> responseDTOs = diplomarbeiten.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    /**
     * Ruft ein einzelnes Projekt anhand seiner ID ab.
     *
     * HTTP-Methode: GET
     * URL: /api/projects/{id}
     */
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<at.ac.tgm.diplomarbeit.diplomdb.dto.DiplomarbeitResponseDTO> getProjectById(@PathVariable Long id) {
        LOGGER.debug("GET /projects/{}", id);
        Diplomarbeit diplomarbeit = diplomarbeitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diplomarbeit nicht gefunden mit ID " + id));
        return ResponseEntity.ok(mapToResponseDTO(diplomarbeit));
    }

    /**
     * Aktualisiert die Daten eines bestehenden Projekts.
     *
     * HTTP-Methode: PUT
     * URL: /api/projects/{id}
     *
     * Hier erfolgt auch die Anpassung der zugewiesenen Projekte-Kapazität:
     * - Wenn der Betreuer geändert wird, wird der alte Lehrer (sofern vorhanden) entsprechend dekrementiert
     *   und der neue Lehrer wird inkrementiert (nach Prüfung der Kapazität).
     * - Wird kein neuer Betreuer gesetzt, so wird ggf. der alte Eintrag entfernt und dessen Zählwert reduziert.
     */
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<at.ac.tgm.diplomarbeit.diplomdb.dto.DiplomarbeitResponseDTO> updateProject(@PathVariable Long id, @RequestBody at.ac.tgm.diplomarbeit.diplomdb.dto.DiplomarbeitDTO diplomarbeitDTO) {
        LOGGER.info("Aktualisiere Projekt: {}", id);
        Diplomarbeit existingDiplomarbeit = diplomarbeitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diplomarbeit nicht gefunden mit ID " + id));

        String oldTeacherSam = existingDiplomarbeit.getBetreuerSamAccountName();
        String newTeacherSam = diplomarbeitDTO.getBetreuerSamAccountName();

        existingDiplomarbeit.setTitel(diplomarbeitDTO.getTitel());
        existingDiplomarbeit.setBeschreibung(diplomarbeitDTO.getBeschreibung());
        existingDiplomarbeit.setStatus(diplomarbeitDTO.getStatus());
        existingDiplomarbeit.setStartdatum(diplomarbeitDTO.getStartdatum());
        existingDiplomarbeit.setEnddatum(diplomarbeitDTO.getEnddatum());

        if (newTeacherSam != null && !newTeacherSam.isEmpty()) {
            if (!userExistsInLdap(newTeacherSam)) {
                throw new ResourceNotFoundException("Betreuer nicht gefunden mit sAMAccountName " + newTeacherSam);
            }
            // Wenn bereits ein anderer Lehrer zugeordnet war, dekrementieren wir dessen Zähler
            if (oldTeacherSam != null && !oldTeacherSam.equalsIgnoreCase(newTeacherSam)) {
                Betreuer oldTeacher = betreuerRepository.findBySamAccountNameIgnoreCase(oldTeacherSam).orElse(null);
                if (oldTeacher != null && oldTeacher.getVergebeneProjekte() != null && oldTeacher.getVergebeneProjekte() > 0) {
                    oldTeacher.setVergebeneProjekte(oldTeacher.getVergebeneProjekte() - 1);
                    betreuerRepository.save(oldTeacher);
                }
            }
            // Bei (Neuzuweisung oder Wechsel) erhöhen wir den Zähler des neuen Lehrers
            Betreuer newTeacher = betreuerRepository.findBySamAccountNameIgnoreCase(newTeacherSam)
                    .orElseGet(() -> betreuerService.findeOderErstelleBetreuer(newTeacherSam));
            if (newTeacher.getVergebeneProjekte() == null) {
                newTeacher.setVergebeneProjekte(0);
            }
            if (newTeacher.getVergebeneProjekte() >= newTeacher.getMaxProjekte()) {
                throw new IllegalStateException("Der Lehrer hat keine freien Kapazitäten.");
            }
            newTeacher.setVergebeneProjekte(newTeacher.getVergebeneProjekte() + 1);
            betreuerRepository.save(newTeacher);
            existingDiplomarbeit.setBetreuerSamAccountName(newTeacherSam);
        } else {
            // Wenn kein neuer Betreuer gesetzt wird, dekrementieren wir ggf. den alten Zähler
            if (oldTeacherSam != null) {
                Betreuer oldTeacher = betreuerRepository.findBySamAccountNameIgnoreCase(oldTeacherSam).orElse(null);
                if (oldTeacher != null && oldTeacher.getVergebeneProjekte() != null && oldTeacher.getVergebeneProjekte() > 0) {
                    oldTeacher.setVergebeneProjekte(oldTeacher.getVergebeneProjekte() - 1);
                    betreuerRepository.save(oldTeacher);
                }
            }
            existingDiplomarbeit.setBetreuerSamAccountName(null);
        }

        if (diplomarbeitDTO.getMitarbeiterSamAccountNames() != null) {
            for (String mitarbSam : diplomarbeitDTO.getMitarbeiterSamAccountNames()) {
                if (!userExistsInLdap(mitarbSam)) {
                    throw new ResourceNotFoundException("Mitarbeiter nicht gefunden mit sAMAccountName " + mitarbSam);
                }
            }
            existingDiplomarbeit.setMitarbeiterSamAccountNames(diplomarbeitDTO.getMitarbeiterSamAccountNames());
        } else {
            existingDiplomarbeit.getMitarbeiterSamAccountNames().clear();
        }

        Diplomarbeit updatedDiplomarbeit = diplomarbeitRepository.save(existingDiplomarbeit);
        AUDIT_LOGGER.info("Projekt aktualisiert: ID={}", id);
        return ResponseEntity.ok(mapToResponseDTO(updatedDiplomarbeit));
    }

    /**
     * Löscht ein Projekt und passt gegebenenfalls die Kapazität des zugewiesenen Lehrers an.
     *
     * HTTP-Methode: DELETE
     * URL: /api/projects/{id}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        LOGGER.warn("Lösche Projekt: {}", id);
        Diplomarbeit diplomarbeit = diplomarbeitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diplomarbeit nicht gefunden mit ID " + id));
        String teacherSam = diplomarbeit.getBetreuerSamAccountName();
        if (teacherSam != null) {
            Betreuer teacher = betreuerRepository.findBySamAccountNameIgnoreCase(teacherSam).orElse(null);
            if (teacher != null && teacher.getVergebeneProjekte() != null && teacher.getVergebeneProjekte() > 0) {
                teacher.setVergebeneProjekte(teacher.getVergebeneProjekte() - 1);
                betreuerRepository.save(teacher);
            }
        }
        diplomarbeitRepository.delete(diplomarbeit);
        AUDIT_LOGGER.info("Projekt gelöscht: ID={}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Hilfsfunktion zur Überprüfung, ob ein Benutzer im LDAP-Verzeichnis existiert.
     */
    private boolean userExistsInLdap(String sAMAccountName) {
        return userService.findBysAMAccountName(sAMAccountName).isPresent();
    }

    /**
     * Wandelt ein Diplomarbeit-Objekt in ein entsprechendes Response-Datentransferobjekt um.
     */
    private DiplomarbeitResponseDTO mapToResponseDTO(Diplomarbeit diplomarbeit) {
        DiplomarbeitResponseDTO dto = new DiplomarbeitResponseDTO();
        dto.setProjektId(diplomarbeit.getProjektId());
        dto.setTitel(diplomarbeit.getTitel());
        dto.setBeschreibung(diplomarbeit.getBeschreibung());
        dto.setStatus(diplomarbeit.getStatus());
        dto.setStartdatum(diplomarbeit.getStartdatum());
        dto.setEnddatum(diplomarbeit.getEnddatum());
        dto.setBetreuerSamAccountName(diplomarbeit.getBetreuerSamAccountName());
        dto.setMitarbeiterSamAccountNames(diplomarbeit.getMitarbeiterSamAccountNames());
        dto.setAblehnungsgrund(diplomarbeit.getAblehnungsgrund());
        return dto;
    }

    /**
     * Exception Handler für IllegalStateException, um passende Fehlermeldungen in der HTTP Response zurückzugeben.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
