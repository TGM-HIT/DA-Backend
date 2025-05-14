package at.ac.tgm.diplomarbeit.diplomdb.controller;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.diplomarbeit.diplomdb.dto.MeilensteinDTO;
import at.ac.tgm.diplomarbeit.diplomdb.entity.Diplomarbeit;
import at.ac.tgm.diplomarbeit.diplomdb.entity.Meilenstein;
import at.ac.tgm.diplomarbeit.diplomdb.exception.ResourceNotFoundException;
import at.ac.tgm.diplomarbeit.diplomdb.mapper.MeilensteinMapper;
import at.ac.tgm.diplomarbeit.diplomdb.repository.DiplomarbeitRepository;
import at.ac.tgm.diplomarbeit.diplomdb.repository.MeilensteinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller zur Verwaltung von Meilensteinen in Projekten.
 *
 * Dieser Controller bietet Endpunkte zur Erstellung, Änderung, Statusaktualisierung, Umbenennung und Löschung von Meilensteinen.
 * Alle Endpunkte sind für die Rollen STUDENT, TEACHER und ADMIN zugänglich, wobei bestimmte Statusänderungen
 * zusätzliche Berechtigungen erfordern.
 *
 * Verfügbare Endpunkte:
 * - POST /api/projects/{projektId}/milestones: Anlegen eines neuen Meilensteins mit dem Status "OFFEN".
 * - GET /api/projects/{projektId}/milestones: Abruf aller Meilensteine zu einem Projekt.
 * - PUT /api/projects/{projektId}/milestones/{milestoneId}/status: Aktualisierung des Status eines Meilensteins.
 * - PUT /api/projects/{projektId}/milestones/{milestoneId}/name: Änderung des Namens eines Meilensteins.
 * - DELETE /api/projects/{projektId}/milestones/{milestoneId}: Löschen eines Meilensteins.
 */
@RestController
@RequestMapping("/diplomdb/api/projects")
public class MeilensteinController {

    @Autowired
    private MeilensteinRepository meilensteinRepository;

    @Autowired
    private DiplomarbeitRepository diplomarbeitRepository;

    /**
     * Erstellt einen neuen Meilenstein für ein bestimmtes Projekt.
     * Der Meilenstein wird initial mit dem Status "OFFEN" angelegt.
     *
     * HTTP-Methode: POST
     * URL: /api/projects/{projektId}/milestones
     *
     * @param projektId Die ID des Projekts, zu dem der Meilenstein gehört.
     * @param meilensteinDTO Das Datentransferobjekt mit den Meilenstein-Daten.
     * @return ResponseEntity mit dem erstellten Meilenstein-Datentransferobjekt.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @PostMapping("/{projektId}/milestones")
    public ResponseEntity<MeilensteinDTO> createMeilenstein(
            @PathVariable Long projektId,
            @RequestBody MeilensteinDTO meilensteinDTO
    ) {
        Diplomarbeit projekt = diplomarbeitRepository.findById(projektId)
                .orElseThrow(() -> new ResourceNotFoundException("Projekt (Diplomarbeit) nicht gefunden mit ID " + projektId));

        Meilenstein neu = new Meilenstein();
        neu.setName(meilensteinDTO.getName());
        neu.setStatus("OFFEN");
        neu.setDiplomarbeit(projekt);

        Meilenstein saved = meilensteinRepository.save(neu);
        return ResponseEntity.status(201).body(MeilensteinMapper.toDTO(saved));
    }

    /**
     * Ruft alle Meilensteine eines bestimmten Projekts ab.
     *
     * HTTP-Methode: GET
     * URL: /api/projects/{projektId}/milestones
     *
     * @param projektId Die ID des Projekts.
     * @return ResponseEntity mit einer Liste von Meilenstein-Datentransferobjekten.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @GetMapping("/{projektId}/milestones")
    public ResponseEntity<List<MeilensteinDTO>> getMilestonesByProject(@PathVariable Long projektId) {
        Diplomarbeit projekt = diplomarbeitRepository.findById(projektId)
                .orElseThrow(() -> new ResourceNotFoundException("Projekt (Diplomarbeit) nicht gefunden mit ID " + projektId));

        List<Meilenstein> meilensteine = meilensteinRepository.findAll().stream()
                .filter(m -> m.getDiplomarbeit().equals(projekt))
                .collect(Collectors.toList());

        List<MeilensteinDTO> dtos = meilensteine.stream()
                .map(MeilensteinMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Aktualisiert den Status eines Meilensteins.
     * Der Status kann auf "IN_BEARBEITUNG", "OFFEN" oder "ERFUELLT" gesetzt werden.
     * Die Änderung auf "ERFUELLT" ist nur für Benutzer mit den Rollen TEACHER oder ADMIN erlaubt.
     *
     * HTTP-Methode: PUT
     * URL: /api/projects/{projektId}/milestones/{milestoneId}/status
     *
     * @param projektId Die ID des Projekts.
     * @param milestoneId Die ID des Meilensteins.
     * @param newStatus Der neue Status, der gesetzt werden soll.
     * @return ResponseEntity mit dem aktualisierten Meilenstein-Datentransferobjekt.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @PutMapping("/{projektId}/milestones/{milestoneId}/status")
    public ResponseEntity<MeilensteinDTO> updateMilestoneStatus(
            @PathVariable Long projektId,
            @PathVariable Long milestoneId,
            @RequestParam("newStatus") String newStatus
    ) {
        Diplomarbeit projekt = diplomarbeitRepository.findById(projektId)
                .orElseThrow(() -> new ResourceNotFoundException("Projekt nicht gefunden mit ID " + projektId));

        Meilenstein meilenstein = meilensteinRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Meilenstein nicht gefunden mit ID " + milestoneId));

        // Sicherstellen, dass der Meilenstein zum angegebenen Projekt gehört.
        if (!meilenstein.getDiplomarbeit().getProjektId().equals(projektId)) {
            throw new IllegalArgumentException("Meilenstein gehört nicht zu diesem Projekt!");
        }

        // Bei Status "ERFUELLT" ist die Berechtigung von Lehrern oder Administratoren erforderlich.
        if ("ERFUELLT".equalsIgnoreCase(newStatus)) {
            boolean isTeacherOrAdmin = checkTeacherOrAdmin();
            if (!isTeacherOrAdmin) {
                throw new SecurityException("Nur Lehrer oder Administratoren dürfen den Meilenstein auf 'ERFUELLT' setzen!");
            }
            meilenstein.setStatus("ERFUELLT");
        } else if ("IN_BEARBEITUNG".equalsIgnoreCase(newStatus)) {
            meilenstein.setStatus("IN_BEARBEITUNG");
        } else if ("OFFEN".equalsIgnoreCase(newStatus)) {
            meilenstein.setStatus("OFFEN");
        } else {
            throw new IllegalArgumentException("Ungültiger Status! Erlaubte Werte: OFFEN, IN_BEARBEITUNG, ERFUELLT.");
        }

        meilensteinRepository.save(meilenstein);
        return ResponseEntity.ok(MeilensteinMapper.toDTO(meilenstein));
    }

    /**
     * Ändert den Namen eines bestehenden Meilensteins.
     * Dies kann zur Korrektur von Tippfehlern oder zur Aktualisierung der Bezeichnung verwendet werden.
     *
     * HTTP-Methode: PUT
     * URL: /api/projects/{projektId}/milestones/{milestoneId}/name
     *
     * @param projektId Die ID des Projekts.
     * @param milestoneId Die ID des Meilensteins.
     * @param newName Der neue Name für den Meilenstein.
     * @return ResponseEntity mit dem aktualisierten Meilenstein-Datentransferobjekt.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @PutMapping("/{projektId}/milestones/{milestoneId}/name")
    public ResponseEntity<MeilensteinDTO> updateMilestoneName(
            @PathVariable Long projektId,
            @PathVariable Long milestoneId,
            @RequestParam("newName") String newName
    ) {
        Diplomarbeit projekt = diplomarbeitRepository.findById(projektId)
                .orElseThrow(() -> new ResourceNotFoundException("Projekt nicht gefunden mit ID " + projektId));

        Meilenstein meilenstein = meilensteinRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Meilenstein nicht gefunden mit ID " + milestoneId));

        // Überprüfen, ob der Meilenstein dem angegebenen Projekt zugeordnet ist.
        if (!meilenstein.getDiplomarbeit().getProjektId().equals(projektId)) {
            throw new IllegalArgumentException("Meilenstein gehört nicht zu diesem Projekt!");
        }

        meilenstein.setName(newName);
        meilensteinRepository.save(meilenstein);

        return ResponseEntity.ok(MeilensteinMapper.toDTO(meilenstein));
    }

    /**
     * Löscht einen Meilenstein aus einem Projekt.
     *
     * HTTP-Methode: DELETE
     * URL: /api/projects/{projektId}/milestones/{milestoneId}
     *
     * @param projektId Die ID des Projekts.
     * @param milestoneId Die ID des zu löschenden Meilensteins.
     * @return ResponseEntity ohne Inhalt, wenn der Löschvorgang erfolgreich war.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @DeleteMapping("/{projektId}/milestones/{milestoneId}")
    public ResponseEntity<Void> deleteMilestone(
            @PathVariable Long projektId,
            @PathVariable Long milestoneId
    ) {
        Diplomarbeit projekt = diplomarbeitRepository.findById(projektId)
                .orElseThrow(() -> new ResourceNotFoundException("Projekt nicht gefunden mit ID " + projektId));

        Meilenstein meilenstein = meilensteinRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Meilenstein nicht gefunden mit ID " + milestoneId));

        // Überprüfen, ob der Meilenstein dem angegebenen Projekt zugeordnet ist.
        if (!meilenstein.getDiplomarbeit().getProjektId().equals(projektId)) {
            throw new IllegalArgumentException("Meilenstein gehört nicht zu diesem Projekt!");
        }

        meilensteinRepository.delete(meilenstein);
        return ResponseEntity.noContent().build();
    }

    /**
     * Überprüft, ob der aktuell authentifizierte Benutzer über die Rollen TEACHER oder ADMIN verfügt.
     *
     * @return true, falls der Benutzer die erforderliche Rolle besitzt, andernfalls false.
     */
    private boolean checkTeacherOrAdmin() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_TEACHER") || a.getAuthority().equals("ROLE_ADMIN")
        );
    }
}
