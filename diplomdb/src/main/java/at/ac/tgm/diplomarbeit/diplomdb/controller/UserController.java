package at.ac.tgm.diplomarbeit.diplomdb.controller;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.diplomarbeit.diplomdb.dto.GroupDTO;
import at.ac.tgm.diplomarbeit.diplomdb.dto.UserDTO;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.entry.GroupEntry;
import at.ac.tgm.ad.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller zur Verwaltung von Benutzer- und LDAP-Abfragen.
 *
 * Dieser Controller bietet Endpunkte zum Testen von LDAP-Benutzerdaten, zum Abruf von Benutzerinformationen
 * sowie zur Suche nach Benutzern anhand von Mail oder Common Name (CN).
 *
 * Verfügbare Endpunkte:
 * - GET /api/test-ldap-user/{samAccountName}: Testet, ob ein LDAP-Benutzer existiert.
 * - GET /api/users/{samAccountName}: Ruft Details zu einem Benutzer ab.
 * - GET /api/users/{samAccountName}/groups: Ruft die Gruppen eines Benutzers ab.
 * - GET /api/users/searchByMail: Sucht einen Benutzer anhand der Mail-Adresse.
 * - GET /api/users/searchByCN: Sucht einen Benutzer anhand des Common Name (CN).
 */
@RestController
@RequestMapping("/diplomdb/api")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("at.ac.tgm.diplomarbeit.diplomdb.audit");

    @Autowired
    private UserService userService;

    /**
     * Testet, ob ein LDAP-Benutzer mit dem angegebenen sAMAccountName existiert.
     *
     * HTTP-Methode: GET
     * URL: /api/test-ldap-user/{samAccountName}
     *
     * @param samAccountName Der sAMAccountName des zu testenden Benutzers.
     * @return ResponseEntity mit einer Bestätigung, ob der Benutzer gefunden wurde.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @GetMapping("/test-ldap-user/{samAccountName}")
    public ResponseEntity<String> testLdapUser(@PathVariable String samAccountName) {
        LOGGER.debug("Test LDAP user: {}", samAccountName);
        return userService.findBysAMAccountName(samAccountName)
                .map(user -> ResponseEntity.ok("User gefunden: " + user.getDisplayName()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Ruft Details zu einem Benutzer anhand des sAMAccountName ab.
     *
     * HTTP-Methode: GET
     * URL: /api/users/{samAccountName}
     *
     * @param samAccountName Der sAMAccountName des Benutzers.
     * @return ResponseEntity mit dem UserDTO, das die Benutzerinformationen enthält.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @GetMapping("/users/{samAccountName}")
    public ResponseEntity<UserDTO> getUserDetails(@PathVariable String samAccountName) {
        LOGGER.debug("GET /users/{}", samAccountName);
        Optional<UserEntry> userOpt = userService.findBysAMAccountNameWithGroups(samAccountName);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDTO = mapToUserDTO(userOpt.get());
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Ruft die Gruppen eines Benutzers anhand seines sAMAccountName ab.
     *
     * HTTP-Methode: GET
     * URL: /api/users/{samAccountName}/groups
     *
     * @param samAccountName Der sAMAccountName des Benutzers.
     * @return ResponseEntity mit einem Set von GroupDTO, das die Gruppeninformationen enthält.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @GetMapping("/users/{samAccountName}/groups")
    public ResponseEntity<Set<GroupDTO>> getUserGroups(@PathVariable String samAccountName) {
        LOGGER.debug("GET /users/{}/groups", samAccountName);
        Optional<UserEntry> userOpt = userService.findBysAMAccountNameWithGroups(samAccountName);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserEntry user = userOpt.get();
        Set<GroupDTO> groupDTOs = user.getGroups().stream()
                .map(this::mapToGroupDTO)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(groupDTOs);
    }

    /**
     * Sucht einen Benutzer anhand der Mail-Adresse.
     *
     * HTTP-Methode: GET
     * URL: /api/users/searchByMail
     *
     * @param mail Die Mail-Adresse, nach der gesucht werden soll.
     * @return ResponseEntity mit dem UserDTO, falls ein Benutzer gefunden wird.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @GetMapping("/users/searchByMail")
    public ResponseEntity<UserDTO> searchUserByMail(@RequestParam String mail) {
        LOGGER.debug("GET /users/searchByMail?mail={}", mail);
        Optional<UserEntry> userOpt = userService.findByMail(mail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<UserEntry> userWithGroups = userService.findBysAMAccountNameWithGroups(userOpt.get().getSAMAccountName());
        if (userWithGroups.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDTO = mapToUserDTO(userWithGroups.get());
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Sucht einen Benutzer anhand des Common Name (CN).
     *
     * HTTP-Methode: GET
     * URL: /api/users/searchByCN
     *
     * @param cn Der Common Name, nach dem gesucht werden soll.
     * @return ResponseEntity mit dem UserDTO, falls ein Benutzer gefunden wird.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @GetMapping("/users/searchByCN")
    public ResponseEntity<UserDTO> searchUserByCN(@RequestParam String cn) {
        LOGGER.debug("GET /users/searchByCN?cn={}", cn);
        Optional<UserEntry> userOpt = userService.findByCommonName(cn, true);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDTO = mapToUserDTO(userOpt.get());
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Wandelt ein UserEntry in ein UserDTO um.
     *
     * @param user Das UserEntry-Objekt.
     * @return Das resultierende UserDTO mit den entsprechenden Benutzerinformationen.
     */
    private UserDTO mapToUserDTO(UserEntry user) {
        UserDTO dto = new UserDTO();
        dto.setSAMAccountName(user.getSAMAccountName());
        dto.setCn(user.getCn());
        dto.setSn(user.getSn());
        dto.setDisplayName(user.getDisplayName());
        dto.setMail(user.getMail());
        if (user.getGroups() != null && !user.getGroups().isEmpty()) {
            Set<GroupDTO> groupDTOs = user.getGroups().stream()
                    .map(this::mapToGroupDTO)
                    .collect(Collectors.toSet());
            dto.setGroups(groupDTOs);
        }
        return dto;
    }

    /**
     * Wandelt ein GroupEntry in ein GroupDTO um.
     *
     * @param group Das GroupEntry-Objekt.
     * @return Das resultierende GroupDTO mit den entsprechenden Gruppendaten.
     */
    private GroupDTO mapToGroupDTO(at.ac.tgm.ad.entry.GroupEntry group) {
        GroupDTO dto = new GroupDTO();
        dto.setCn(group.getCn());
        dto.setDisplayName(group.getDisplayName());
        return dto;
    }
}
