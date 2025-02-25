package at.ac.tgm.diplomarbeit.diplomdb.service;

import at.ac.tgm.diplomarbeit.diplomdb.entity.Schueler;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.diplomarbeit.diplomdb.repository.SchuelerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service-Klasse zur Verwaltung der Schülerdaten.
 * Diese Klasse ermöglicht den Import von Schülerdaten aus dem LDAP (Clean Slate)
 * und das Abrufen der Schülerliste mit optionalen Filter- und Sortierkriterien.
 */
@Service
public class SchuelerService {

    /**
     * Logger zur Protokollierung von Ereignissen in dieser Klasse.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SchuelerService.class);

    @Autowired
    private SchuelerRepository schuelerRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private LdapTemplate ldapTemplate;

    /**
     * Aktualisiert die Schülerliste für einen bestimmten Jahrgang (4 oder 5) aus dem LDAP.
     * Dabei werden zunächst alle vorhandenen Schülerdaten gelöscht (Clean Slate) und anschließend
     * die aktuellen Schülerdaten aus dem LDAP importiert.
     *
     * @param year Der Jahrgang, für den die Schülerdaten importiert werden sollen ("4" oder "5").
     */
    public void refreshSchuelerListFromLDAP(String year) {
        LOGGER.info("Aktualisiere Schülerliste aus LDAP für Jahrgang {}", year);

        // 1) Lösche alle vorhandenen Schülerdaten
        schuelerRepository.deleteAll();

        // 2) Erzeuge den LDAP-Filter basierend auf dem Jahrgang
        String filter = createHITSchuelerGroupFilter(year);

        // 3) Sammle alle sAMAccountNames, die dem Filter entsprechen, mithilfe der Paging-Methode
        List<String> allSAMs = collectAllSAMAccountNamesPaged(filter);

        // 4) Für jeden sAMAccountName: Hole die LDAP-Daten und speichere einen neuen Schülereintrag
        int count = 0;
        for (String sam : allSAMs) {
            userService.findBysAMAccountNameWithGroups(sam).ifPresent(user -> {
                Schueler s = Schueler.builder()
                        .samAccountName(sam)
                        .vorname(user.getGivenName())
                        .nachname(user.getSn())
                        .email(user.getMail())
                        .displayName(user.getDisplayName())
                        .build();
                schuelerRepository.save(s);
            });
            count++;
        }
        LOGGER.info("Schüler-Refresh abgeschlossen. Gesamt gefundene sAMs={}, gespeichert={}", allSAMs.size(), count);
    }

    /**
     * Erzeugt einen LDAP-Filter für Schüler des angegebenen Jahrgangs.
     * Für Jahrgang 4 bzw. 5 werden die entsprechenden Gruppen-CNs berücksichtigt.
     *
     * @param year Der Jahrgang ("4" oder "5").
     * @return Der LDAP-Filter als String.
     */
    private String createHITSchuelerGroupFilter(String year) {
        if ("4".equals(year)) {
            return "(&" +
                    "(objectClass=user)" +
                    "(|" +
                    "(memberOf=CN=schueler4AHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at)" +
                    "(memberOf=CN=schueler4BHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at)" +
                    "(memberOf=CN=schueler4CHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at)" +
                    "(memberOf=CN=schueler4DHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at)" +
                    ")" +
                    ")";
        } else if ("5".equals(year)) {
            return "(&" +
                    "(objectClass=user)" +
                    "(|" +
                    "(memberOf=CN=schueler5AHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at)" +
                    "(memberOf=CN=schueler5BHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at)" +
                    "(memberOf=CN=schueler5CHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at)" +
                    "(memberOf=CN=schueler5DHIT,OU=Groups,OU=tgm,DC=tgm,DC=ac,DC=at)" +
                    ")" +
                    ")";
        }
        return "(objectClass=none)";
    }

    /**
     * Sammelt alle sAMAccountNames aus dem LDAP, die dem angegebenen Filter entsprechen, mithilfe der Paging-Methode.
     *
     * @param filter Der LDAP-Filter, der angewendet werden soll.
     * @return Eine Liste der gesammelten sAMAccountNames.
     */
    private List<String> collectAllSAMAccountNamesPaged(String filter) {
        List<String> result = new ArrayList<>();
        String baseDn = "";
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(new String[]{"sAMAccountName"});

        int pageSize = 200;
        PagedResultsCookie cookie = null;

        do {
            PagedResultsDirContextProcessor pager = new PagedResultsDirContextProcessor(pageSize, cookie);
            List<String> pageSAMs = ldapTemplate.search(
                    baseDn,
                    filter,
                    controls,
                    (AttributesMapper<String>) this::extractSamAccountName,
                    pager
            );
            result.addAll(pageSAMs);
            cookie = pager.getCookie();
        } while (cookie != null && cookie.getCookie() != null);

        return result;
    }

    /**
     * Extrahiert den sAMAccountName aus den LDAP-Attributen.
     *
     * @param attrs Die LDAP-Attribute.
     * @return Der extrahierte sAMAccountName oder null, falls das Attribut nicht vorhanden ist.
     * @throws NamingException Falls ein Fehler beim Zugriff auf die Attribute auftritt.
     */
    private String extractSamAccountName(Attributes attrs) throws NamingException {
        if (attrs.get("sAMAccountName") != null) {
            return attrs.get("sAMAccountName").get().toString();
        }
        return null;
    }

    /**
     * Sucht die Schüler in der Datenbank anhand eines optionalen Suchbegriffs und sortiert sie
     * anhand des angegebenen Sortierfeldes und der Sortierrichtung.
     *
     * @param search Optionaler Suchbegriff (z. B. Vorname, Nachname, Email, sAMAccountName).
     * @param sortField Das Feld, nach dem sortiert werden soll (z. B. "nachname", "vorname", "samAccountName").
     * @param sortDirection Sortierrichtung ("asc" oder "desc").
     * @return Eine Liste der Schüler, die den Filter- und Sortierkriterien entsprechen.
     */
    public List<Schueler> searchSchueler(String search, String sortField, String sortDirection) {
        List<Schueler> all = schuelerRepository.findAll();

        // Filtere Schüler anhand des Suchbegriffs in Vorname, Nachname, Email und sAMAccountName
        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase();
            all = all.stream().filter(s ->
                    (s.getVorname() != null && s.getVorname().toLowerCase().contains(lower))
                            || (s.getNachname() != null && s.getNachname().toLowerCase().contains(lower))
                            || (s.getEmail() != null && s.getEmail().toLowerCase().contains(lower))
                            || (s.getSamAccountName() != null && s.getSamAccountName().toLowerCase().contains(lower))
            ).collect(Collectors.toList());
        }

        // Sortiere die Schüler anhand des angegebenen Sortierfeldes
        Comparator<Schueler> comparator;
        if ("vorname".equalsIgnoreCase(sortField)) {
            comparator = Comparator.comparing(Schueler::getVorname,
                    Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
        } else if ("samaccountname".equalsIgnoreCase(sortField)) {
            comparator = Comparator.comparing(Schueler::getSamAccountName,
                    Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
        } else {
            // Standardmäßig nach Nachname sortieren
            comparator = Comparator.comparing(Schueler::getNachname,
                    Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        all.sort(comparator);
        return all;
    }
}
