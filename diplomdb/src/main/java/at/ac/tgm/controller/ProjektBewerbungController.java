package at.ac.tgm.controller;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.entity.ProjektBewerbung;
import at.ac.tgm.service.ProjektBewerbungService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static at.ac.tgm.Consts.DIPLOMDB_PATH_PREFIX;

/**
 * Controller zur Verwaltung von Projekt-Bewerbungen.
 *
 * Dieser Controller stellt Endpunkte zur Verfügung, um Bewerbungen für Projekte zu erstellen, abzurufen,
 * eine Übersicht gruppiert nach Benutzern anzuzeigen und Bewerbungen zu löschen.
 * (Die Funktionen zum Akzeptieren/Ablehnen von Bewerbungen wurden entfernt.)
 *
 * Verfügbare Endpunkte:
 * - POST /api/project-applications: Erstellung einer neuen Bewerbung.
 * - GET /api/project-applications: Abruf von Bewerbungen mit optionalen Filterkriterien.
 * - GET /api/project-applications/overview: Gruppierte Übersicht der Bewerbungen pro Benutzer.
 * - DELETE /api/project-applications/{id}: Löschen einer Bewerbung – nur für Administratoren.
 */
@RestController
@RequestMapping(DIPLOMDB_PATH_PREFIX + "/api/project-applications")
public class ProjektBewerbungController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjektBewerbungController.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("at.ac.tgm.diplomarbeit.diplomdb.audit");

    @Autowired
    private ProjektBewerbungService bewerbungService;

    /**
     * Erstellt eine neue Bewerbung für ein Projekt.
     * Bei Bewerbungen von Studenten wird der sAMAccountName des aktuell angemeldeten Benutzers erzwungen.
     * Lehrer und Administratoren können im Request einen beliebigen sAMAccountName angeben.
     *
     * HTTP-Methode: POST
     * URL: /api/project-applications
     *
     * @param bewerbung Das Bewerbung-Objekt aus dem Request-Body.
     * @return ResponseEntity mit dem erstellten Bewerbung-Objekt.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary     = "Erstellt eine neue Bewerbung für ein Projekt.",
            description = "Bei Studenten wird der sAMAccountName auf den aktuell angemeldeten Benutzer gesetzt. Lehrer und Administratoren können im Request einen beliebigen sAMAccountName angeben.",
            responses   = {
                    @ApiResponse(responseCode = "201", description = "Bewerbung erfolgreich erstellt"),
                    @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten"),
                    @ApiResponse(responseCode = "500", description = "Interner Serverfehler")
            }
    )
    @PostMapping
    public ResponseEntity<ProjektBewerbung> createApplication(@RequestBody ProjektBewerbung bewerbung) {
        LOGGER.info("Erstelle Bewerbung: projektId={}, user(angefragt)={}, prioritaet={}",
                bewerbung.getProjektId(), bewerbung.getSamAccountName(), bewerbung.getPrioritaet());

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!isTeacherOrAdmin()) {
            // Bei Studenten wird der sAMAccountName auf den des aktuell angemeldeten Benutzers gesetzt.
            bewerbung.setSamAccountName(currentUser);
        }

        ProjektBewerbung created = bewerbungService.createBewerbung(bewerbung);

        AUDIT_LOGGER.info("Bewerbung erstellt: id={}, projektId={}, finalSam={}, prioritaet={}, erstelltDurch={}",
                created.getBewerbungId(),
                created.getProjektId(),
                created.getSamAccountName(),
                created.getPrioritaet(),
                currentUser);

        return ResponseEntity.status(201).body(created);
    }

    /**
     * Ruft Bewerbungen ab, wobei optionale Filterkriterien wie Benutzer, Projekt-ID, Projektname und Sortierung berücksichtigt werden.
     *
     * HTTP-Methode: GET
     * URL: /api/project-applications
     *
     * @param user Optionaler Filter nach Benutzer (sAMAccountName).
     * @param projektId Optionaler Filter nach Projekt-ID.
     * @param projektName Optionaler Filter nach Projektname.
     * @param sortBy Optionales Sortierkriterium.
     * @return ResponseEntity mit einer Liste von Bewerbungen.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary    = "Ruft Bewerbungen ab, optional gefiltert nach Benutzer, Projekt-ID, Projektname und sortiert.",
            parameters = {
                    @Parameter(
                            name        = "user",
                            in          = ParameterIn.QUERY,
                            description = "Optionaler Filter nach Benutzer (sAMAccountName)",
                            required    = false
                    ),
                    @Parameter(
                            name        = "projektId",
                            in          = ParameterIn.QUERY,
                            description = "Optionaler Filter nach Projekt-ID",
                            required    = false
                    ),
                    @Parameter(
                            name        = "projektName",
                            in          = ParameterIn.QUERY,
                            description = "Optionaler Filter nach Projektname",
                            required    = false
                    ),
                    @Parameter(
                            name        = "sortBy",
                            in          = ParameterIn.QUERY,
                            description = "Optionales Sortierkriterium",
                            required    = false
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "200", description = "Liste der gefundenen Bewerbungen")
            }
    )
    @GetMapping
    public ResponseEntity<List<ProjektBewerbung>> getApplications(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) Long projektId,
            @RequestParam(required = false) String projektName,
            @RequestParam(required = false) String sortBy
    ) {
        LOGGER.debug("GET /project-applications => user={}, projektId={}, projektName={}, sortBy={}",
                user, projektId, projektName, sortBy);
        List<ProjektBewerbung> list = bewerbungService.searchBewerbungen(user, projektId, projektName, sortBy);
        return ResponseEntity.ok(list);
    }

    /**
     * Gibt eine gruppierte Übersicht aller Bewerbungen pro Benutzer zurück.
     * Dies ermöglicht es, beispielsweise die 1., 2. und 3. Wunschbewerbung eines Schülers zu sehen.
     *
     * HTTP-Methode: GET
     * URL: /api/project-applications/overview
     *
     * @return ResponseEntity mit einer Liste von UserBewerbungOverviewDTO, gruppiert nach Benutzer.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary   = "Gibt eine gruppierte Übersicht aller Bewerbungen pro Benutzer zurück.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste von UserBewerbungOverviewDTO, gruppiert nach Benutzer")
            }
    )
    @GetMapping("/overview")
    public ResponseEntity<List<ProjektBewerbungService.UserBewerbungOverviewDTO>> getApplicationsOverview() {
        LOGGER.debug("GET /project-applications/overview");
        List<ProjektBewerbungService.UserBewerbungOverviewDTO> overview = bewerbungService.getGroupedByUser();
        return ResponseEntity.ok(overview);
    }

    /**
     * Löscht eine Bewerbung.
     * Dieser Endpunkt ist ausschließlich für Administratoren zugänglich.
     *
     * HTTP-Methode: DELETE
     * URL: /api/project-applications/{id}
     *
     * @param id Die ID der zu löschenden Bewerbung.
     * @return ResponseEntity ohne Inhalt, wenn die Löschung erfolgreich war.
     */
    @Secured(Roles.ADMIN)
    @Operation(
            summary    = "Löscht eine Bewerbung.",
            parameters = {
                    @Parameter(
                            name        = "id",
                            in          = ParameterIn.PATH,
                            description = "ID der zu löschenden Bewerbung",
                            required    = true
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "204", description = "Bewerbung erfolgreich gelöscht"),
                    @ApiResponse(responseCode = "404", description = "Bewerbung nicht gefunden")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        LOGGER.warn("Lösche Bewerbung: id={}", id);
        bewerbungService.deleteBewerbung(id);
        AUDIT_LOGGER.info("Bewerbung gelöscht: bewerbungId={}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Hilfsfunktion, die überprüft, ob der aktuell authentifizierte Benutzer über die Rolle TEACHER oder ADMIN verfügt.
     *
     * @return true, wenn der Benutzer TEACHER oder ADMIN ist, andernfalls false.
     */
    private boolean isTeacherOrAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String role = ga.getAuthority();
            if ("ROLE_TEACHER".equals(role) || "ROLE_ADMIN".equals(role)) {
                return true;
            }
        }
        return false;
    }
}
