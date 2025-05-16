package at.ac.tgm.diplomarbeit.diplomdb.controller;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.diplomarbeit.diplomdb.dto.DokumentDTO;
import at.ac.tgm.diplomarbeit.diplomdb.entity.Diplomarbeit;
import at.ac.tgm.diplomarbeit.diplomdb.entity.Dokument;
import at.ac.tgm.diplomarbeit.diplomdb.exception.ResourceNotFoundException;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.diplomarbeit.diplomdb.repository.DokumentRepository;
import at.ac.tgm.diplomarbeit.diplomdb.repository.DiplomarbeitRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.PathResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Controller zur Verwaltung von Dokumenten.
 *
 * Dieser Controller stellt Endpunkte zur Verfügung, um Dokumente hochzuladen, herunterzuladen,
 * abzurufen, zu aktualisieren, zu bewerten und zu löschen. Die Funktionalitäten sind für die Rollen
 * STUDENT, TEACHER und ADMIN zugänglich, wobei bestimmte Aktionen (wie die Bewertung) zusätzlichen
 * Berechtigungen unterliegen.
 *
 * Verfügbare Endpunkte:
 * - POST /api/documents/upload: Upload eines neuen Dokuments.
 * - GET /api/documents/download/{id}: Download eines Dokuments anhand seiner ID.
 * - GET /api/documents: Abruf einer Liste von Dokumenten mit optionaler Filterung und Sortierung.
 * - GET /api/documents/{id}: Abruf eines spezifischen Dokuments anhand seiner ID.
 * - PUT /api/documents/{id}: Aktualisierung der Dokumentendaten.
 * - PUT /api/documents/{id}/rating: Aktualisierung der Bewertung eines Dokuments.
 * - DELETE /api/documents/{id}: Löschen eines Dokuments und der zugehörigen Datei.
 */
@RestController
@RequestMapping("/diplomdb/api/documents")
public class DokumentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DokumentController.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("at.ac.tgm.diplomarbeit.diplomdb.audit");

    @Autowired
    private DokumentRepository dokumentRepository;

    @Autowired
    private DiplomarbeitRepository diplomarbeitRepository;

    @Autowired
    private UserService userService;

    private static final String UPLOAD_DIR = "uploads/";

    /**
     * Erlaubt den Upload eines Dokuments.
     *
     * HTTP-Methode: POST
     * URL: /api/documents/upload
     *
     * @param file Die hochzuladende Datei.
     * @param titel Der Titel des Dokuments.
     * @param beschreibung Eine Beschreibung des Dokuments.
     * @param typ Der Typ des Dokuments (z.B. Lastenheft, Design-Dokument).
     * @param datum Das Datum des Dokuments im Format YYYY-MM-DD.
     * @param diplomarbeitId Die ID der zugehörigen Diplomarbeit.
     * @param erstellerSamAccountName Der sAMAccountName des Erstellers.
     * @return ResponseEntity mit dem erstellten Dokument-Datentransferobjekt.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary     = "Erlaubt den Upload eines Dokuments.",
            description = "Erwartet Multipart-Form-Daten mit Datei und Metadaten zum Dokument.",
            parameters  = {
                    @Parameter(name = "file",                     in = ParameterIn.QUERY,  description = "Die hochzuladende Datei",                         required = true),
                    @Parameter(name = "titel",                    in = ParameterIn.QUERY,  description = "Der Titel des Dokuments",                           required = true),
                    @Parameter(name = "beschreibung",              in = ParameterIn.QUERY,  description = "Eine Beschreibung des Dokuments",                   required = true),
                    @Parameter(name = "typ",                      in = ParameterIn.QUERY,  description = "Der Typ des Dokuments (z.B. Lastenheft, Design)",    required = true),
                    @Parameter(name = "datum",                    in = ParameterIn.QUERY,  description = "Das Datum des Dokuments im Format YYYY-MM-DD",      required = true),
                    @Parameter(name = "diplomarbeitId",           in = ParameterIn.QUERY,  description = "Die ID der zugehörigen Diplomarbeit",               required = true),
                    @Parameter(name = "erstellerSamAccountName",  in = ParameterIn.QUERY,  description = "Der sAMAccountName des Erstellers",                 required = true)
            },
            responses   = {
                    @ApiResponse(responseCode = "201", description = "Dokument erfolgreich hochgeladen"),
                    @ApiResponse(responseCode = "500", description = "Interner Serverfehler")
            }
    )
    @PostMapping("/upload")
    public ResponseEntity<DokumentDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("titel") String titel,
            @RequestParam("beschreibung") String beschreibung,
            @RequestParam("typ") String typ,
            @RequestParam("datum") String datum,
            @RequestParam("diplomarbeitId") Long diplomarbeitId,
            @RequestParam("erstellerSamAccountName") String erstellerSamAccountName) {

        LOGGER.info("Dokument-Upload: titel={}, diplomarbeitId={}", titel, diplomarbeitId);
        if (!userExistsInLdap(erstellerSamAccountName)) {
            throw new ResourceNotFoundException("Ersteller (LDAP User) nicht gefunden mit sAMAccountName " + erstellerSamAccountName);
        }

        try {
            Dokument dokument = new Dokument();
            dokument.setTitel(titel);
            dokument.setBeschreibung(beschreibung);
            dokument.setTyp(typ);
            dokument.setHochladungsdatum(LocalDate.now());
            dokument.setDatum(LocalDate.parse(datum));
            dokument.setDateiname(file.getOriginalFilename());

            String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            dokument.setDateipfad(filePath.toString());

            Diplomarbeit diplomarbeit = diplomarbeitRepository.findById(diplomarbeitId)
                    .orElseThrow(() -> new ResourceNotFoundException("Diplomarbeit nicht gefunden mit ID " + diplomarbeitId));
            dokument.setDiplomarbeit(diplomarbeit);

            dokument.setErstellerSamAccountName(erstellerSamAccountName);

            // Initialisierung der Bewertungsfelder
            dokument.setBewertungProzent(null);
            dokument.setBewertetDurchSamAccountName(null);
            dokument.setBewertungsKommentar(null);

            Dokument savedDokument = dokumentRepository.save(dokument);
            DokumentDTO dokumentDTO = mapToDTO(savedDokument);

            AUDIT_LOGGER.info("Dokument hochgeladen: dokumentId={}, diplomarbeitId={}, user={}",
                    savedDokument.getDokumentId(), diplomarbeitId, erstellerSamAccountName);

            return ResponseEntity.status(HttpStatus.CREATED).body(dokumentDTO);
        } catch (IOException e) {
            LOGGER.error("Fehler beim Hochladen eines Dokuments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Ermöglicht den Download eines Dokuments anhand seiner ID.
     *
     * HTTP-Methode: GET
     * URL: /api/documents/download/{id}
     *
     * @param id Die eindeutige ID des Dokuments.
     * @return ResponseEntity mit dem herunterzuladenden Resource-Objekt.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary    = "Ermöglicht den Download eines Dokuments anhand seiner ID.",
            parameters = {
                    @Parameter(
                            name        = "id",
                            in          = ParameterIn.PATH,
                            description = "Die eindeutige ID des Dokuments",
                            required    = true
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "200", description = "Dokument erfolgreich heruntergeladen"),
                    @ApiResponse(responseCode = "404", description = "Dokument oder Datei nicht gefunden"),
                    @ApiResponse(responseCode = "500", description = "Interner Serverfehler")
            }
    )
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        LOGGER.debug("Download Dokument: {}", id);
        Dokument dokument = dokumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dokument nicht gefunden mit ID " + id));

        Path filePath = Paths.get(dokument.getDateipfad());
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Datei nicht gefunden auf dem Server");
        }

        try {
            Resource resource = new PathResource(filePath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + dokument.getDateiname() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(filePath))
                    .body(resource);
        } catch (IOException e) {
            LOGGER.error("Fehler beim Download des Dokuments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Ruft alle Dokumente ab, optional gefiltert nach Suchbegriff, Typ und Datum sowie sortiert nach einem angegebenen Feld.
     *
     * HTTP-Methode: GET
     * URL: /api/documents
     *
     * @param search Optionaler Suchbegriff.
     * @param typ Optionaler Dokumenttyp.
     * @param vonDatum Optionales Startdatum (YYYY-MM-DD).
     * @param bisDatum Optionales Enddatum (YYYY-MM-DD).
     * @param sortField Feld zur Sortierung; Standard ist "dokumentId".
     * @param sortDirection Sortierrichtung ("asc" oder "desc"); Standard ist "asc".
     * @return ResponseEntity mit einer Liste von Dokument-Datentransferobjekten.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary    = "Ruft alle Dokumente ab, optional gefiltert nach Suchbegriff, Typ und Datum sowie sortiert.",
            parameters = {
                    @Parameter(
                            name        = "search",
                            in          = ParameterIn.QUERY,
                            description = "Optionaler Suchbegriff",
                            required    = false
                    ),
                    @Parameter(
                            name        = "typ",
                            in          = ParameterIn.QUERY,
                            description = "Optionaler Dokumenttyp",
                            required    = false
                    ),
                    @Parameter(
                            name        = "vonDatum",
                            in          = ParameterIn.QUERY,
                            description = "Startdatum für den Datumsfilter (YYYY-MM-DD)",
                            required    = false
                    ),
                    @Parameter(
                            name        = "bisDatum",
                            in          = ParameterIn.QUERY,
                            description = "Enddatum für den Datumsfilter (YYYY-MM-DD)",
                            required    = false
                    ),
                    @Parameter(
                            name        = "sortField",
                            in          = ParameterIn.QUERY,
                            description = "Feld zur Sortierung; Standard ist \"dokumentId\"",
                            required    = false,
                            schema      = @Schema(type = "string", defaultValue = "dokumentId")
                    ),
                    @Parameter(
                            name        = "sortDirection",
                            in          = ParameterIn.QUERY,
                            description = "Sortierrichtung (\"asc\" oder \"desc\"); Standard ist \"asc\"",
                            required    = false,
                            schema      = @Schema(type = "string", defaultValue = "asc")
                    )
            },
            responses  = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "Liste der Dokumente entsprechend der Filter- und Sortierkriterien"
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<DokumentDTO>> getAllDocuments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String typ,
            @RequestParam(required = false) String vonDatum,
            @RequestParam(required = false) String bisDatum,
            @RequestParam(required = false, defaultValue = "dokumentId") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {

        LOGGER.debug("GET /documents => search={}, typ={}, vonDatum={}, bisDatum={}, sortField={}, sortDirection={}",
                search, typ, vonDatum, bisDatum, sortField, sortDirection);

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortField).descending() :
                Sort.by(sortField).ascending();

        List<Dokument> dokumente = dokumentRepository.findAll(sort);

        if (typ != null && !typ.isEmpty()) {
            dokumente = dokumente.stream()
                    .filter(d -> d.getTyp() != null && d.getTyp().equalsIgnoreCase(typ))
                    .collect(Collectors.toList());
        }

        if (vonDatum != null && !vonDatum.isEmpty() && bisDatum != null && !bisDatum.isEmpty()) {
            LocalDate von = LocalDate.parse(vonDatum);
            LocalDate bis = LocalDate.parse(bisDatum);
            dokumente = dokumente.stream()
                    .filter(d -> d.getDatum() != null &&
                            !d.getDatum().isBefore(von) &&
                            !d.getDatum().isAfter(bis))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isEmpty()) {
            String lowerCaseSearch = search.toLowerCase();
            dokumente = dokumente.stream()
                    .filter(d -> (d.getTitel() != null && d.getTitel().toLowerCase().contains(lowerCaseSearch))
                            || (d.getBeschreibung() != null && d.getBeschreibung().toLowerCase().contains(lowerCaseSearch))
                            || (d.getErstellerSamAccountName() != null && d.getErstellerSamAccountName().toLowerCase().contains(lowerCaseSearch))
                            || (d.getDiplomarbeit() != null && d.getDiplomarbeit().getTitel() != null
                            && d.getDiplomarbeit().getTitel().toLowerCase().contains(lowerCaseSearch)))
                    .collect(Collectors.toList());
        }

        List<DokumentDTO> dokumentDTOs = dokumente.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dokumentDTOs);
    }

    /**
     * Ruft ein einzelnes Dokument anhand seiner ID ab.
     *
     * HTTP-Methode: GET
     * URL: /api/documents/{id}
     *
     * @param id Die eindeutige ID des Dokuments.
     * @return ResponseEntity mit dem Dokument-Datentransferobjekt.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary    = "Ruft ein spezifisches Dokument anhand seiner ID ab.",
            parameters = {
                    @Parameter(
                            name        = "id",
                            in          = ParameterIn.PATH,
                            description = "Die eindeutige ID des Dokuments",
                            required    = true
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "200", description = "Dokument erfolgreich zurückgegeben"),
                    @ApiResponse(responseCode = "404", description = "Dokument nicht gefunden")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<DokumentDTO> getDocumentById(@PathVariable Long id) {
        LOGGER.debug("GET /documents/{}", id);
        Dokument dokument = dokumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dokument nicht gefunden mit ID " + id));
        return ResponseEntity.ok(mapToDTO(dokument));
    }

    /**
     * Aktualisiert die Daten eines bestehenden Dokuments.
     *
     * HTTP-Methode: PUT
     * URL: /api/documents/{id}
     *
     * @param id Die eindeutige ID des Dokuments.
     * @param dokumentDTO Das Datentransferobjekt mit den aktualisierten Dokumentendaten.
     * @return ResponseEntity mit dem aktualisierten Dokument-Datentransferobjekt.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary     = "Aktualisiert die Daten eines bestehenden Dokuments.",
            parameters  = {
                    @Parameter(
                            name        = "id",
                            in          = ParameterIn.PATH,
                            description = "ID des Dokuments",
                            required    = true
                    )
            },
            responses   = {
                    @ApiResponse(responseCode = "200", description = "Dokument erfolgreich aktualisiert"),
                    @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten"),
                    @ApiResponse(responseCode = "404", description = "Dokument nicht gefunden")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<DokumentDTO> updateDocument(@PathVariable Long id, @RequestBody DokumentDTO dokumentDTO) {
        LOGGER.info("Update Dokument: {}", id);
        Dokument existingDokument = dokumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dokument nicht gefunden mit ID " + id));

        existingDokument.setTitel(dokumentDTO.getTitel());
        existingDokument.setBeschreibung(dokumentDTO.getBeschreibung());
        existingDokument.setTyp(dokumentDTO.getTyp());
        existingDokument.setDatum(dokumentDTO.getDatum());
        existingDokument.setDateiname(dokumentDTO.getDateiname());

        if (dokumentDTO.getDiplomarbeitId() != null) {
            Diplomarbeit diplomarbeit = diplomarbeitRepository.findById(dokumentDTO.getDiplomarbeitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Diplomarbeit nicht gefunden mit ID " + dokumentDTO.getDiplomarbeitId()));
            existingDokument.setDiplomarbeit(diplomarbeit);
        } else {
            existingDokument.setDiplomarbeit(null);
        }

        if (dokumentDTO.getErstellerSamAccountName() != null && !dokumentDTO.getErstellerSamAccountName().isEmpty()) {
            if (!userExistsInLdap(dokumentDTO.getErstellerSamAccountName())) {
                throw new ResourceNotFoundException("Ersteller (LDAP User) nicht gefunden mit sAMAccountName " + dokumentDTO.getErstellerSamAccountName());
            }
            existingDokument.setErstellerSamAccountName(dokumentDTO.getErstellerSamAccountName());
        } else {
            existingDokument.setErstellerSamAccountName(null);
        }

        existingDokument.setBewertungProzent(dokumentDTO.getBewertungProzent());
        existingDokument.setBewertetDurchSamAccountName(dokumentDTO.getBewertetDurchSamAccountName());
        existingDokument.setBewertungsKommentar(dokumentDTO.getBewertungsKommentar());

        Dokument updatedDokument = dokumentRepository.save(existingDokument);
        LOGGER.info("Dokument aktualisiert: dokumentId={}", id);
        return ResponseEntity.ok(mapToDTO(updatedDokument));
    }

    /**
     * Aktualisiert die Bewertung eines Dokuments.
     *
     * HTTP-Methode: PUT
     * URL: /api/documents/{id}/rating
     *
     * @param id Die eindeutige ID des Dokuments.
     * @param value Der Bewertungsscore (zwischen 0 und 100).
     * @param comment Optionaler Kommentar zur Bewertung.
     * @return ResponseEntity mit dem aktualisierten Dokument-Datentransferobjekt.
     */
    @Secured({Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary    = "Aktualisiert die Bewertung eines Dokuments.",
            parameters = {
                    @Parameter(
                            name        = "id",
                            in          = ParameterIn.PATH,
                            description = "ID des Dokuments",
                            required    = true
                    ),
                    @Parameter(
                            name        = "value",
                            in          = ParameterIn.QUERY,
                            description = "Bewertungsscore (0–100)",
                            required    = true,
                            schema      = @Schema(type = "integer")
                    ),
                    @Parameter(
                            name        = "comment",
                            in          = ParameterIn.QUERY,
                            description = "Optionaler Kommentar zur Bewertung",
                            required    = false
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "200", description = "Dokument-Bewertung erfolgreich aktualisiert"),
                    @ApiResponse(responseCode = "400", description = "Ungültige Bewertung oder Eingabedaten"),
                    @ApiResponse(responseCode = "404", description = "Dokument nicht gefunden")
            }
    )
    @PutMapping("/{id}/rating")
    public ResponseEntity<DokumentDTO> updateDocumentRating(
            @PathVariable Long id,
            @RequestParam("value") Integer value,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        LOGGER.info("Aktualisiere Dokument-Bewertung: dokumentId={}, value={}, comment={}", id, value, comment);
        if (value == null || value < 0 || value > 100) {
            throw new IllegalArgumentException("Bewertung muss zwischen 0 und 100 liegen!");
        }

        Dokument dokument = dokumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dokument nicht gefunden mit ID " + id));

        dokument.setBewertungProzent(value);
        String currentUserSam = SecurityContextHolder.getContext().getAuthentication().getName();
        dokument.setBewertetDurchSamAccountName(currentUserSam);

        if (comment != null && !comment.isBlank()) {
            dokument.setBewertungsKommentar(comment.trim());
        } else {
            dokument.setBewertungsKommentar(null);
        }

        Dokument saved = dokumentRepository.save(dokument);
        AUDIT_LOGGER.info("Dokument bewertet: dokumentId={}, user={}, value={}", id, currentUserSam, value);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    /**
     * Löscht ein Dokument. Dabei wird auch die zugehörige Datei vom Server entfernt.
     *
     * HTTP-Methode: DELETE
     * URL: /api/documents/{id}
     *
     * @param id Die eindeutige ID des zu löschenden Dokuments.
     * @return ResponseEntity ohne Inhalt, wenn das Löschen erfolgreich war.
     */
    @Secured(Roles.ADMIN)
    @Operation(
            summary    = "Löscht ein Dokument und entfernt die zugehörige Datei vom Server.",
            parameters = {
                    @Parameter(
                            name        = "id",
                            in          = ParameterIn.PATH,
                            description = "ID des zu löschenden Dokuments",
                            required    = true
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "204", description = "Dokument erfolgreich gelöscht"),
                    @ApiResponse(responseCode = "404", description = "Dokument nicht gefunden")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        LOGGER.warn("Lösche Dokument: {}", id);
        Dokument dokument = dokumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dokument nicht gefunden mit ID " + id));

        // Überprüfe, ob der Dateipfad vorhanden ist, bevor versucht wird, die Datei zu löschen
        if (dokument.getDateipfad() != null && !dokument.getDateipfad().isEmpty()) {
            try {
                Path filePath = Paths.get(dokument.getDateipfad());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                LOGGER.error("Fehler beim Löschen der Datei", e);
            }
        }
        dokumentRepository.delete(dokument);
        AUDIT_LOGGER.info("Dokument gelöscht: dokumentId={}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Wandelt ein Dokument-Objekt in ein DokumentDTO um.
     *
     * @param dokument Das zu konvertierende Dokument.
     * @return Das resultierende DokumentDTO.
     */
    private DokumentDTO mapToDTO(Dokument dokument) {
        DokumentDTO dto = new DokumentDTO();
        dto.setDokumentId(dokument.getDokumentId());
        dto.setTitel(dokument.getTitel());
        dto.setBeschreibung(dokument.getBeschreibung());
        dto.setTyp(dokument.getTyp());
        dto.setHochladungsdatum(dokument.getHochladungsdatum());
        dto.setDatum(dokument.getDatum());
        dto.setDateiname(dokument.getDateiname());

        if (dokument.getDiplomarbeit() != null) {
            dto.setDiplomarbeitId(dokument.getDiplomarbeit().getProjektId());
            dto.setDiplomarbeitTitel(dokument.getDiplomarbeit().getTitel());
        }

        dto.setErstellerSamAccountName(dokument.getErstellerSamAccountName());
        dto.setBewertungProzent(dokument.getBewertungProzent());
        dto.setBewertetDurchSamAccountName(dokument.getBewertetDurchSamAccountName());
        dto.setBewertungsKommentar(dokument.getBewertungsKommentar());

        return dto;
    }

    /**
     * Hilfsfunktion zur Überprüfung, ob ein Benutzer im LDAP-Verzeichnis existiert.
     *
     * @param sAMAccountName Der sAMAccountName des Benutzers.
     * @return true, falls der Benutzer existiert, andernfalls false.
     */
    private boolean userExistsInLdap(String sAMAccountName) {
        return userService.findBysAMAccountName(sAMAccountName).isPresent();
    }
}
