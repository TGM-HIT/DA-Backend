package at.ac.tgm.diplomarbeit.diplomdb.controller;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.diplomarbeit.diplomdb.entity.Schueler;
import at.ac.tgm.diplomarbeit.diplomdb.service.SchuelerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller zur Verwaltung der Schülerdaten.
 *
 * Dieser Controller bietet Endpunkte zum Aktualisieren (LDAP-Import mit Clean Slate) und zum Abrufen
 * der Schülerliste. Die Aktualisierung berücksichtigt den Jahrgang (4 oder 5) und löscht vor dem Import
 * alle vorhandenen Einträge.
 *
 * Verfügbare Endpunkte:
 * - POST /api/schueler/refresh: Löscht alle bestehenden Schülerdaten und importiert neue Daten aus dem LDAP-Verzeichnis.
 *   Erfordert den Parameter "year" mit den Werten "4" oder "5".
 * - GET /api/schueler: Ruft die Schülerliste ab, optional gefiltert nach einem Suchbegriff und sortiert nach einem angegebenen Feld.
 */
@RestController
@RequestMapping("/api/schueler")
public class SchuelerController {

    @Autowired
    private SchuelerService schuelerService;

    /**
     * Aktualisiert die Schülerliste für den angegebenen Jahrgang durch einen Neuimport aus dem LDAP-Verzeichnis.
     * Vorher werden alle vorhandenen Einträge gelöscht (Clean Slate).
     *
     * HTTP-Methode: POST
     * URL: /api/schueler/refresh?year=4 oder /api/schueler/refresh?year=5
     *
     * @param year Der Jahrgang, für den die Schülerdaten importiert werden sollen ("4" oder "5").
     * @return ResponseEntity mit einer Bestätigungsmeldung oder einer Fehlermeldung bei ungültigem Jahrgang.
     */
    @Secured(Roles.ADMIN)
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshSchuelerList(@RequestParam String year) {
        if (!("4".equals(year) || "5".equals(year))) {
            return ResponseEntity.badRequest().body("Ungültiger Parameter year, nur '4' oder '5' erlaubt.");
        }
        schuelerService.refreshSchuelerListFromLDAP(year);
        return ResponseEntity.ok("Schuelerliste für Jahrgang " + year + " (Clean Slate) aktualisiert.");
    }

    /**
     * Ruft die Schülerliste ab, optional gefiltert nach einem Suchbegriff und sortiert nach einem angegebenen Feld.
     *
     * HTTP-Methode: GET
     * URL: /api/schueler
     *
     * @param search Optionaler Suchbegriff zur Filterung der Schüler.
     * @param sortField Das Feld, nach dem sortiert wird (z.B. "nachname", "vorname", "samAccountName"). Standard ist "nachname".
     * @param sortDirection Die Sortierrichtung, entweder "asc" (aufsteigend) oder "desc" (absteigend). Standard ist "asc".
     * @return ResponseEntity mit einer Liste der Schüler, die den angegebenen Filter- und Sortierkriterien entsprechen.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @GetMapping
    public ResponseEntity<List<Schueler>> getSchueler(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "nachname") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection
    ) {
        List<Schueler> result = schuelerService.searchSchueler(search, sortField, sortDirection);
        return ResponseEntity.ok(result);
    }
}
