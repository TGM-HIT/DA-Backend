package at.ac.tgm.diplomarbeit.diplomdb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Service-Klasse zur Verwaltung der Administratorenliste.
 * Diese Klasse lädt die Administratoren (sAMAccountNames) aus einer JSON-Datei
 * und stellt eine Methode zur Verfügung, mit der überprüft werden kann, ob ein bestimmter
 * Benutzer als Administrator definiert ist.
 */
@Service
public class AdminListService {

    /**
     * Logger zur Protokollierung von Ereignissen in dieser Klasse.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminListService.class);

    /**
     * Menge der sAMAccountNames, die als Administratoren definiert sind.
     * Diese Menge wird beim Start der Anwendung aus der JSON-Konfiguration geladen.
     */
    private Set<String> adminSams = Collections.emptySet();

    /**
     * Diese Methode wird nach der Initialisierung der Bean automatisch aufgerufen.
     * Sie lädt die Administratorenliste aus der Datei "config/admins.json".
     */
    @PostConstruct
    public void loadAdminsFromJson() {
        try {
            LOGGER.info("Lade Admin-Liste aus JSON ...");
            ClassPathResource resource = new ClassPathResource("config/admins.json");
            if (!resource.exists()) {
                LOGGER.warn("config/admins.json nicht gefunden! Es wurden keine Administratoren konfiguriert.");
                return;
            }
            try (InputStream in = resource.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(in);
                JsonNode adminsNode = root.get("admins");
                if (adminsNode != null && adminsNode.isArray()) {
                    Set<String> set = new HashSet<>();
                    for (JsonNode node : adminsNode) {
                        if (node.isTextual()) {
                            set.add(node.asText().toLowerCase());
                        }
                    }
                    this.adminSams = set;
                }
            }
            LOGGER.info("AdminListService - geladene Admins: {}", this.adminSams);
        } catch (Exception e) {
            LOGGER.error("Fehler beim Laden der Admin-Liste", e);
        }
    }

    /**
     * Überprüft, ob der übergebene sAMAccountName zu den Administratoren gehört.
     *
     * @param samAccountName Der sAMAccountName des zu überprüfenden Benutzers.
     * @return true, wenn der Benutzer als Administrator definiert ist, andernfalls false.
     */
    public boolean isAdmin(String samAccountName) {
        if (samAccountName == null) return false;
        return adminSams.contains(samAccountName.toLowerCase());
    }
}
