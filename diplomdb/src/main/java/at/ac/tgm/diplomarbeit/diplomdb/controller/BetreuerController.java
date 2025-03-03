package at.ac.tgm.diplomarbeit.diplomdb.controller;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.diplomarbeit.diplomdb.entity.Betreuer;
import at.ac.tgm.diplomarbeit.diplomdb.service.BetreuerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller zur Verwaltung der Betreuer.
 *
 * Dieser Controller bietet Endpunkte zum Aktualisieren und Abrufen der Betreuerliste,
 * zum Ändern des Status sowie zur Anpassung der Projektkapazität und zum Exportieren der Daten.
 *
 * Verfügbare Endpunkte:
 * - POST /api/betreuer/refresh:
 *   Aktualisiert die Betreuerliste durch einen Import aus dem LDAP-Verzeichnis.
 * - GET /api/betreuer:
 *   Ruft die Liste der Betreuer ab, optional gefiltert nach Suchbegriff, Status und
 *   sortiert nach einem angegebenen Feld.
 * - PUT /api/betreuer/sam/{samAccountName}/status:
 *   Ändert den Status eines spezifischen Betreuers, identifiziert durch den sAMAccountName.
 * - PUT /api/betreuer/capacity/self:
 *   Ermöglicht einem Lehrer, seine eigene maximale Anzahl betreuter Projekte anzupassen.
 * - PUT /api/betreuer/sam/{samAccountName}/capacity:
 *   Erlaubt einem Administrator, die maximale Anzahl betreuter Projekte für einen Betreuer festzulegen.
 * - GET /api/betreuer/export:
 *   Exportiert die Betreuerliste in den Formaten CSV, Excel oder PDF. Es können optionale Filter-
 *   und Sortierparameter übergeben werden.
 */
@RestController
@RequestMapping("/api/betreuer")
public class BetreuerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BetreuerController.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("at.ac.tgm.diplomarbeit.diplomdb.audit");

    @Autowired
    private BetreuerService betreuerService;

    /**
     * Aktualisiert die Betreuerliste aus dem LDAP-Verzeichnis.
     * Nur Benutzer mit der Rolle ADMIN haben Zugriff.
     *
     * HTTP-Methode: POST
     * URL: /api/betreuer/refresh
     *
     * @return ResponseEntity mit einer Bestätigungsmeldung, dass die Betreuerliste erfolgreich aktualisiert wurde.
     */
    @Secured(Roles.ADMIN)
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshBetreuerList() {
        LOGGER.info("Refreshing Betreuer list from LDAP...");
        betreuerService.refreshBetreuerListFromLDAP();
        AUDIT_LOGGER.info("Die Betreuerliste wurde aus dem LDAP-Verzeichnis aktualisiert (Admin-Aktion).");
        return ResponseEntity.ok("Betreuerliste wurde aktualisiert.");
    }

    /**
     * Ruft eine Liste von Betreuern ab. Es können optionale Parameter zur Filterung und Sortierung angegeben werden.
     *
     * HTTP-Methode: GET
     * URL: /api/betreuer
     *
     * @param search Optionaler Suchbegriff zur Filterung der Betreuer.
     * @param sortField Das Feld, nach dem sortiert wird; Standard ist "id".
     * @param sortDirection Die Sortierrichtung ("asc" oder "desc"); Standard ist "asc".
     * @param status Optionaler Filter für den Status.
     * @return ResponseEntity mit der Liste der Betreuer, die den angegebenen Kriterien entsprechen.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @GetMapping
    public ResponseEntity<List<Betreuer>> getBetreuer(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "id") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String status
    ) {
        LOGGER.debug("GET /api/betreuer => search={}, status={}, sortField={}, sortDirection={}",
                search, status, sortField, sortDirection);
        List<Betreuer> result = betreuerService.searchBetreuer(search, sortField, sortDirection, status);
        return ResponseEntity.ok(result);
    }

    /**
     * Aktualisiert den Status eines spezifischen Betreuers anhand des sAMAccountName.
     * Nur Benutzer mit den Rollen TEACHER und ADMIN dürfen diesen Endpunkt aufrufen.
     *
     * HTTP-Methode: PUT
     * URL: /api/betreuer/sam/{samAccountName}/status
     *
     * @param samAccountName Der sAMAccountName des Betreuers, dessen Status geändert werden soll.
     * @param newStatus Der neue Status, der gesetzt werden soll.
     * @return ResponseEntity mit dem aktualisierten Betreuer-Objekt.
     */
    @Secured({Roles.TEACHER, Roles.ADMIN})
    @PutMapping("/sam/{samAccountName}/status")
    public ResponseEntity<Betreuer> setBetreuerStatusBySam(
            @PathVariable String samAccountName,
            @RequestParam String newStatus
    ) {
        LOGGER.info("Setze neuen Status für Betreuer (samAccountName={}) auf: {}", samAccountName, newStatus);
        Betreuer updated = betreuerService.updateBetreuerStatusBySam(samAccountName, newStatus);
        AUDIT_LOGGER.info("Betreuer-Status aktualisiert: samAccountName={}, neuer Status={}", samAccountName, newStatus);
        return ResponseEntity.ok(updated);
    }

    /**
     * Aktualisiert die eigene Kapazität eines Lehrers.
     * Dieser Endpunkt ist ausschließlich für Benutzer mit der Rolle TEACHER zugänglich.
     *
     * HTTP-Methode: PUT
     * URL: /api/betreuer/capacity/self
     *
     * @param maxProjekte Die neue maximale Anzahl von Projekten, die der Lehrer betreuen kann.
     * @return ResponseEntity mit dem aktualisierten Betreuer-Objekt.
     */
    @Secured(Roles.TEACHER)
    @PutMapping("/capacity/self")
    public ResponseEntity<Betreuer> updateOwnCapacity(
            @RequestParam int maxProjekte
    ) {
        String currentUser = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        LOGGER.info("Lehrer {} aktualisiert eigene Kapazität auf: maxProjekte={}", currentUser, maxProjekte);
        Betreuer updated = betreuerService.updateBetreuerCapacityBySam(currentUser, maxProjekte);
        AUDIT_LOGGER.info("Betreuerkapazität von Lehrer {} wurde aktualisiert auf: maxProjekte={}", currentUser, maxProjekte);
        return ResponseEntity.ok(updated);
    }

    /**
     * Aktualisiert die Kapazität eines spezifischen Betreuers.
     * Dieser Endpunkt ist ausschließlich für Administratoren vorgesehen.
     *
     * HTTP-Methode: PUT
     * URL: /api/betreuer/sam/{samAccountName}/capacity
     *
     * @param samAccountName Der sAMAccountName des Betreuers, dessen Kapazität geändert werden soll.
     * @param maxProjekte Die neue maximale Anzahl von Projekten, die der Betreuer betreuen darf.
     * @return ResponseEntity mit dem aktualisierten Betreuer-Objekt.
     */
    @Secured(Roles.ADMIN)
    @PutMapping("/sam/{samAccountName}/capacity")
    public ResponseEntity<Betreuer> updateTeacherCapacity(
            @PathVariable String samAccountName,
            @RequestParam int maxProjekte
    ) {
        LOGGER.info("Administrator aktualisiert Kapazität für Betreuer (samAccountName={}) auf: maxProjekte={}", samAccountName, maxProjekte);
        Betreuer updated = betreuerService.updateBetreuerCapacityBySam(samAccountName, maxProjekte);
        AUDIT_LOGGER.info("Betreuerkapazität durch Admin aktualisiert: samAccountName={}, maxProjekte={}", samAccountName, maxProjekte);
        return ResponseEntity.ok(updated);
    }

    /**
     * Exportiert die Betreuerliste in einem der unterstützten Formate: CSV, Excel oder PDF.
     * Es können optionale Such-, Sortier- und Statusfilter als Parameter übergeben werden.
     *
     * HTTP-Methode: GET
     * URL: /api/betreuer/export
     *
     * @param format Das gewünschte Exportformat ("csv", "excel" oder "pdf").
     * @param search Optionaler Suchbegriff zur Filterung.
     * @param sortField Das Feld, nach dem sortiert wird; Standard ist "id".
     * @param sortDirection Die Sortierrichtung ("asc" oder "desc"); Standard ist "asc".
     * @param status Optionaler Filter für den Status.
     * @return ResponseEntity, die die exportierten Daten als Byte-Array enthält und entsprechende HTTP-Header gesetzt hat.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportBetreuerList(
            @RequestParam String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "id") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String status
    ) {
        LOGGER.info("Exportiere Betreuerliste im Format: {}", format);
        try {
            byte[] data;
            String contentType;
            String filename;
            switch (format.toLowerCase()) {
                case "csv":
                    data = betreuerService.exportBetreuerListAsCsv(search, sortField, sortDirection, status);
                    contentType = "text/csv";
                    filename = "betreuer_list.csv";
                    break;
                case "excel":
                    data = betreuerService.exportBetreuerListAsExcel(search, sortField, sortDirection, status);
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    filename = "betreuer_list.xlsx";
                    break;
                case "pdf":
                    data = betreuerService.exportBetreuerListAsPdf(search, sortField, sortDirection, status);
                    contentType = "application/pdf";
                    filename = "betreuer_list.pdf";
                    break;
                default:
                    return ResponseEntity.badRequest().body(null);
            }

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(data.length);

            LOGGER.info("Export der Betreuerliste erfolgreich abgeschlossen (Format: {}, Größe: {} Bytes)", format, data.length);
            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            LOGGER.error("Fehler beim Export der Betreuerliste", e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
