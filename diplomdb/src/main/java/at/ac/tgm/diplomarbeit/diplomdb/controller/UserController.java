package at.ac.tgm.diplomarbeit.diplomdb.controller;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.diplomarbeit.diplomdb.dto.GroupDTO;
import at.ac.tgm.diplomarbeit.diplomdb.dto.UserDTO;
import at.ac.tgm.diplomarbeit.diplomdb.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static at.ac.tgm.Consts.DIPLOMDB_PATH_PREFIX;

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
@RequestMapping(DIPLOMDB_PATH_PREFIX + "/api")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("at.ac.tgm.diplomarbeit.diplomdb.audit");

    @Autowired
    private UserService userService;

    /**
     * Testet, ob ein LDAP-Benutzer mit dem angegebenen sAMAccountName existiert.
     * <p>
     * HTTP-Methode: GET
     * URL: /api/test-ldap-user/{samAccountName}
     *
     * @param samAccountName Der sAMAccountName des zu testenden Benutzers.
     * @return ResponseEntity mit einer Bestätigung, ob der Benutzer gefunden wurde.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary    = "Testet, ob ein LDAP-Benutzer mit dem angegebenen sAMAccountName existiert.",
            parameters = {
                    @Parameter(
                            name        = "samAccountName",
                            in          = ParameterIn.PATH,
                            description = "sAMAccountName des zu testenden Benutzers",
                            required    = true
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "200", description = "User gefunden"),
                    @ApiResponse(responseCode = "404", description = "User nicht gefunden")
            }
    )
    @GetMapping("/test-ldap-user/{samAccountName}")
    public ResponseEntity<String> testLdapUser(@PathVariable String samAccountName) {
        LOGGER.debug("Test LDAP user: {}", samAccountName);
        return userService.findBysAMAccountName(samAccountName)
                .map(user -> ResponseEntity.ok("User gefunden: " + user.getDisplayName()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Ruft Details zu einem Benutzer anhand des sAMAccountName ab.
     * <p>
     * HTTP-Methode: GET
     * URL: /api/users/{samAccountName}
     *
     * @param samAccountName Der sAMAccountName des Benutzers.
     * @return ResponseEntity mit dem UserDTO, das die Benutzerinformationen enthält.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary    = "Ruft Details zu einem Benutzer anhand des sAMAccountName ab.",
            parameters = {
                    @Parameter(
                            name        = "samAccountName",
                            in          = ParameterIn.PATH,
                            description = "sAMAccountName des Benutzers",
                            required    = true
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "200", description = "Benutzerdaten erfolgreich zurückgegeben"),
                    @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden")
            }
    )
    @GetMapping("/users/{samAccountName}")
    public ResponseEntity<UserDTO> getUserDetails(@PathVariable String samAccountName) {
        LOGGER.debug("GET /users/{}", samAccountName);
        Optional<UserEntry> userOpt = userService.findBysAMAccountNameWithGroups(samAccountName);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDTO = UserMapper.toDTO(userOpt.get());
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Ruft die Gruppen eines Benutzers anhand seines sAMAccountName ab.
     * <p>
     * HTTP-Methode: GET
     * URL: /api/users/{samAccountName}/groups
     *
     * @param samAccountName Der sAMAccountName des Benutzers.
     * @return ResponseEntity mit einem Set von GroupDTO, das die Gruppeninformationen enthält.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary    = "Ruft die Gruppen eines Benutzers anhand seines sAMAccountName ab.",
            parameters = {
                    @Parameter(
                            name        = "samAccountName",
                            in          = ParameterIn.PATH,
                            description = "sAMAccountName des Benutzers",
                            required    = true
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "200", description = "Gruppen des Benutzers erfolgreich zurückgegeben"),
                    @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden")
            }
    )
    @GetMapping("/users/{samAccountName}/groups")
    public ResponseEntity<Set<GroupDTO>> getUserGroups(@PathVariable String samAccountName) {
        LOGGER.debug("GET /users/{}/groups", samAccountName);
        Optional<UserEntry> userOpt = userService.findBysAMAccountNameWithGroups(samAccountName);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserEntry user = userOpt.get();
        Set<GroupDTO> groupDTOs = user.getGroups().stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(groupDTOs);
    }

    /**
     * Sucht einen Benutzer anhand der Mail-Adresse.
     * <p>
     * HTTP-Methode: GET
     * URL: /api/users/searchByMail
     *
     * @param mail Die Mail-Adresse, nach der gesucht werden soll.
     * @return ResponseEntity mit dem UserDTO, falls ein Benutzer gefunden wird.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary    = "Sucht einen Benutzer anhand der Mail-Adresse.",
            parameters = {
                    @Parameter(
                            name        = "mail",
                            in          = ParameterIn.QUERY,
                            description = "Die Mail-Adresse, nach der gesucht werden soll",
                            required    = true
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "200", description = "Benutzer erfolgreich gefunden"),
                    @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden")
            }
    )
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
        UserDTO userDTO = UserMapper.toDTO(userWithGroups.get());
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Sucht einen Benutzer anhand des Common Name (CN).
     * <p>
     * HTTP-Methode: GET
     * URL: /api/users/searchByCN
     *
     * @param cn Der Common Name, nach dem gesucht werden soll.
     * @return ResponseEntity mit dem UserDTO, falls ein Benutzer gefunden wird.
     */
    @Secured({Roles.STUDENT, Roles.TEACHER, Roles.ADMIN})
    @Operation(
            summary    = "Sucht einen Benutzer anhand des Common Name (CN).",
            parameters = {
                    @Parameter(
                            name        = "cn",
                            in          = ParameterIn.QUERY,
                            description = "Der Common Name, nach dem gesucht werden soll",
                            required    = true
                    )
            },
            responses  = {
                    @ApiResponse(responseCode = "200", description = "Benutzer erfolgreich gefunden"),
                    @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden")
            }
    )
    @GetMapping("/users/searchByCN")
    public ResponseEntity<UserDTO> searchUserByCN(@RequestParam String cn) {
        LOGGER.debug("GET /users/searchByCN?cn={}", cn);
        Optional<UserEntry> userOpt = userService.findByCommonName(cn, true);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserDTO userDTO = UserMapper.toDTO(userOpt.get());
        return ResponseEntity.ok(userDTO);
    }

}