package at.ac.tgm.diplomarbeit.diplomdb.service;

import at.ac.tgm.diplomarbeit.diplomdb.entity.Diplomarbeit;
import at.ac.tgm.diplomarbeit.diplomdb.entity.ProjektBewerbung;
import at.ac.tgm.diplomarbeit.diplomdb.exception.ResourceNotFoundException;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.diplomarbeit.diplomdb.repository.DiplomarbeitRepository;
import at.ac.tgm.diplomarbeit.diplomdb.repository.ProjektBewerbungRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service-Klasse zur Verwaltung von Projekt-Bewerbungen.
 * Diese Klasse bietet Funktionen zum Erstellen, Suchen und Löschen von Bewerbungen
 * sowie zur Erstellung einer gruppierten Übersicht der Bewerbungen nach Benutzer.
 */
@Service
public class ProjektBewerbungService {

    /**
     * Logger zur Protokollierung von Ereignissen in dieser Klasse.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjektBewerbungService.class);

    @Autowired
    private ProjektBewerbungRepository bewerbungRepository;

    @Autowired
    private DiplomarbeitRepository diplomarbeitRepository;

    @Autowired
    private UserService userService;

    /**
     * Erstellt eine neue Bewerbung für ein Projekt.
     * Es wird geprüft, ob das zugehörige Projekt existiert, ob der LDAP-Benutzer vorhanden ist,
     * ob die Priorität gültig ist und ob bereits eine Bewerbung mit derselben Priorität existiert.
     * Falls eine Bewerbung mit derselben Priorität bereits vorhanden ist, wird diese aktualisiert.
     * Falls nicht, wird geprüft, ob der Benutzer bereits 3 Bewerbungen hat.
     *
     * @param bewerbung Die zu erstellende Bewerbung.
     * @return Die erstellte oder aktualisierte Bewerbung.
     * @throws ResourceNotFoundException Falls das Projekt oder der LDAP-Benutzer nicht gefunden werden.
     * @throws IllegalArgumentException Falls die Priorität ungültig ist.
     * @throws IllegalStateException Falls bereits 3 Bewerbungen existieren.
     */
    public ProjektBewerbung createBewerbung(ProjektBewerbung bewerbung) {
        LOGGER.info("Erstelle Bewerbung: projektId={}, user={}, prioritaet={}, team={}",
                bewerbung.getProjektId(),
                bewerbung.getSamAccountName(),
                bewerbung.getPrioritaet(),
                bewerbung.getTeamMitglieder());

        // Überprüfe, ob das zugehörige Projekt existiert
        Diplomarbeit projekt = diplomarbeitRepository.findById(bewerbung.getProjektId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projekt (Diplomarbeit) nicht gefunden mit ID " + bewerbung.getProjektId()));

        // Prüfe, ob das Projekt nicht geschlossen ist
        if ("GESCHLOSSEN".equalsIgnoreCase(projekt.getStatus())) {
            throw new IllegalStateException("Dieses Projekt ist geschlossen, Bewerbung nicht möglich.");
        }

        // Überprüfe, ob der LDAP-Benutzer existiert
        userService.findBysAMAccountName(bewerbung.getSamAccountName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LDAP-User nicht gefunden mit samAccountName=" + bewerbung.getSamAccountName()));

        // Validierung der Priorität (muss 1, 2 oder 3 sein)
        if (bewerbung.getPrioritaet() == null || bewerbung.getPrioritaet() < 1 || bewerbung.getPrioritaet() > 3) {
            throw new IllegalArgumentException("Priorität muss 1, 2 oder 3 sein!");
        }

        // Prüfe, ob bereits eine Bewerbung mit derselben Priorität existiert
        Optional<ProjektBewerbung> existingBewerbungOpt = bewerbungRepository.findByProjektIdAndSamAccountNameIgnoreCaseAndPrioritaet(
                bewerbung.getProjektId(), bewerbung.getSamAccountName(), bewerbung.getPrioritaet()
        );
        if (existingBewerbungOpt.isPresent()) {
            // Aktualisiere die bestehende Bewerbung mit den neuen Daten
            ProjektBewerbung existing = existingBewerbungOpt.get();
            existing.setTeamMitglieder(bewerbung.getTeamMitglieder());
            // Weitere Felder können hier bei Bedarf aktualisiert werden
            existing.setEntscheidungsStatus("PENDING");
            ProjektBewerbung updated = bewerbungRepository.save(existing);
            LOGGER.info("Bestehende Bewerbung mit Priorität {} aktualisiert.", bewerbung.getPrioritaet());
            return updated;
        } else {
            // Überprüfe, ob der Benutzer bereits 3 Bewerbungen hat
            long existingCount = bewerbungRepository.findAll().stream()
                    .filter(b -> b.getSamAccountName().equalsIgnoreCase(bewerbung.getSamAccountName()))
                    .count();
            if (existingCount >= 3) {
                throw new IllegalStateException("Du hast bereits 3 Bewerbungen angelegt. Mehr sind nicht möglich.");
            }
            // Setze den Entscheidungsstatus auf "PENDING" und speichere die Bewerbung
            bewerbung.setEntscheidungsStatus("PENDING");
            ProjektBewerbung saved = bewerbungRepository.save(bewerbung);
            return saved;
        }
    }

    /**
     * Sucht Bewerbungen anhand verschiedener Filterkriterien, wie sAMAccountName, Projekt-ID und Projektname.
     * Optional kann nach einem bestimmten Sortierkriterium sortiert werden.
     *
     * @param samAccountName Optionaler Filter nach sAMAccountName.
     * @param projektId Optionaler Filter nach Projekt-ID.
     * @param projektName Optionaler Filter nach Projektname.
     * @param sortBy Optionales Sortierkriterium (z. B. "displayName").
     * @return Eine Liste von Bewerbungen, die den angegebenen Kriterien entsprechen.
     */
    public List<ProjektBewerbung> searchBewerbungen(
            String samAccountName,
            Long projektId,
            String projektName,
            String sortBy
    ) {
        LOGGER.debug("Suche Bewerbungen: user={}, projektId={}, projektName={}, sortBy={}",
                samAccountName, projektId, projektName, sortBy);

        List<ProjektBewerbung> all = bewerbungRepository.findAll();

        if (samAccountName != null && !samAccountName.isBlank()) {
            all = all.stream()
                    .filter(b -> b.getSamAccountName().equalsIgnoreCase(samAccountName))
                    .collect(Collectors.toList());
        }

        if (projektId != null) {
            all = all.stream()
                    .filter(b -> b.getProjektId().equals(projektId))
                    .collect(Collectors.toList());
        }

        if (projektName != null && !projektName.isBlank()) {
            String lower = projektName.toLowerCase();
            List<Diplomarbeit> gefundeneProjekte = diplomarbeitRepository.findAll().stream()
                    .filter(d -> d.getTitel() != null && d.getTitel().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
            Set<Long> matchingIds = gefundeneProjekte.stream()
                    .map(Diplomarbeit::getProjektId)
                    .collect(Collectors.toSet());
            all = all.stream()
                    .filter(b -> matchingIds.contains(b.getProjektId()))
                    .collect(Collectors.toList());
        }

        // Sortiere die Bewerbungen entweder anhand des DisplayNames (aus LDAP) oder standardmäßig nach der Bewerbung-ID
        if ("displayName".equalsIgnoreCase(sortBy)) {
            all.sort(Comparator.comparing(b -> {
                return userService.findBysAMAccountName(b.getSamAccountName())
                        .map(u -> u.getDisplayName() != null ? u.getDisplayName() : u.getCn())
                        .orElse(b.getSamAccountName());
            }, String.CASE_INSENSITIVE_ORDER));
        } else {
            all.sort(Comparator.comparing(ProjektBewerbung::getBewerbungId));
        }

        return all;
    }

    /**
     * Löscht eine Bewerbung anhand der übergebenen ID.
     *
     * @param id Die ID der zu löschenden Bewerbung.
     * @throws ResourceNotFoundException Falls die Bewerbung nicht gefunden wird.
     */
    public void deleteBewerbung(Long id) {
        LOGGER.warn("Lösche Bewerbung: {}", id);
        ProjektBewerbung pb = bewerbungRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bewerbung nicht gefunden mit ID " + id));
        bewerbungRepository.delete(pb);
    }

    /**
     * Erstellt eine gruppierte Übersicht der Bewerbungen, gruppiert nach dem sAMAccountName des Bewerbers.
     * Für jeden Benutzer werden die zugehörigen Bewerbungen sowie der DisplayName (aus LDAP) ermittelt.
     *
     * @return Eine Liste von UserBewerbungOverviewDTO, die die gruppierte Übersicht enthält.
     */
    public List<UserBewerbungOverviewDTO> getGroupedByUser() {
        List<ProjektBewerbung> all = bewerbungRepository.findAll();

        // Gruppiere Bewerbungen nach sAMAccountName (in Kleinbuchstaben)
        Map<String, List<ProjektBewerbung>> grouped = all.stream()
                .collect(Collectors.groupingBy(b -> b.getSamAccountName().toLowerCase()));

        List<UserBewerbungOverviewDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<ProjektBewerbung>> entry : grouped.entrySet()) {
            String sam = entry.getKey();
            List<ProjektBewerbung> bewerbungen = entry.getValue();

            // Ermittele den DisplayName aus LDAP (falls vorhanden)
            String displayName = sam;
            var ldapOpt = userService.findBysAMAccountName(sam);
            if (ldapOpt.isPresent() && ldapOpt.get().getDisplayName() != null) {
                displayName = ldapOpt.get().getDisplayName();
            }

            UserBewerbungOverviewDTO dto = new UserBewerbungOverviewDTO();
            dto.setSamAccountName(sam);
            dto.setDisplayName(displayName);
            dto.setBewerbungen(bewerbungen);
            result.add(dto);
        }

        // Sortiere die Übersicht alphabetisch nach dem DisplayName
        result.sort(Comparator.comparing(UserBewerbungOverviewDTO::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    /**
     * Inneres DTO zur Darstellung der gruppierten Bewerbungsübersicht pro Benutzer.
     */
    public static class UserBewerbungOverviewDTO {
        private String samAccountName;
        private String displayName;
        private List<ProjektBewerbung> bewerbungen;

        public String getSamAccountName() {
            return samAccountName;
        }

        public void setSamAccountName(String samAccountName) {
            this.samAccountName = samAccountName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public List<ProjektBewerbung> getBewerbungen() {
            return bewerbungen;
        }

        public void setBewerbungen(List<ProjektBewerbung> bewerbungen) {
            this.bewerbungen = bewerbungen;
        }
    }
}
