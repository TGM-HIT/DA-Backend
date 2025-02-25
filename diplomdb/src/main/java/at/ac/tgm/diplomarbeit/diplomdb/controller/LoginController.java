package at.ac.tgm.diplomarbeit.diplomdb.controller;

import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.diplomarbeit.diplomdb.service.AdminListService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller zur Authentifizierung und Sitzungsverwaltung.
 *
 * Dieser Controller bietet Endpunkte zur Benutzeranmeldung, zum Abmelden sowie zum Abrufen des CSRF-Tokens.
 * Es wird eine Rate-Limiting-Logik implementiert, um wiederholte fehlgeschlagene Loginversuche einzuschränken.
 *
 * Verfügbare Endpunkte:
 * - POST /api/login: Authentifiziert einen Benutzer anhand der übermittelten Anmeldedaten und erstellt eine Session.
 * - POST /api/logout: Beendet die aktuelle Benutzersitzung.
 * - GET /api/csrf-token: Gibt den aktuellen CSRF-Token zurück, der für nachfolgende Anfragen benötigt wird.
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LoginController.class);
    private static final org.slf4j.Logger AUDIT_LOGGER = org.slf4j.LoggerFactory.getLogger("at.ac.tgm.diplomarbeit.diplomdb.audit");

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminListService adminListService;

    /**
     * In-Memory-Map für fehlgeschlagene Loginversuche (Rate Limiting).
     */
    private static final Map<String, List<Long>> failedLoginsMap = new ConcurrentHashMap<>();
    private static final int MAX_LOGIN_FAILURES = 10;
    private static final long TIME_WINDOW_MILLIS = 5 * 60_000; // 5 Minuten

    /**
     * Authentifiziert einen Benutzer anhand der übermittelten Anmeldedaten.
     *
     * Bei erfolgreicher Authentifizierung wird der Benutzer im SecurityContext gespeichert,
     * eine Session angelegt und der Benutzer erhält seine Rolle (ROLE_STUDENT, ROLE_TEACHER oder ROLE_ADMIN).
     *
     * HTTP-Methode: POST
     * URL: /api/login
     *
     * @param credentials Eine Map, die die Schlüssel "username" und "password" enthält.
     * @param request Das HttpServletRequest zur Erfassung der IP-Adresse und zur Session-Erstellung.
     * @return Eine Map mit Status, Nachricht, Benutzernamen und zugewiesener Rolle.
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Audit: Login-Versuch
        AUDIT_LOGGER.info("LoginAttempt user={}, ip={}", username, request.getRemoteAddr());

        if (isUserBlocked(username)) {
            AUDIT_LOGGER.warn("LoginBlocked user={}, reason=too_many_failed_attempts", username);
            return Map.of(
                    "status", "failure",
                    "message", "Zu viele fehlgeschlagene Versuche. Bitte warte 5 Minuten."
            );
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            resetFailedLogins(username);
            SecurityContextHolder.getContext().setAuthentication(auth);

            String resolvedRole = determineUserRole(username);
            List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<>();
            updatedAuthorities.add(new SimpleGrantedAuthority(resolvedRole));

            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    auth.getPrincipal(), auth.getCredentials(), updatedAuthorities
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
            session.setAttribute("userRole", resolvedRole);

            // Audit: Erfolg
            AUDIT_LOGGER.info("LoginSuccess user={}, role={}", username, resolvedRole);

            return Map.of(
                    "status", "success",
                    "message", "Login erfolgreich",
                    "username", username,
                    "role", resolvedRole
            );
        } catch (AuthenticationException e) {
            recordFailedLogin(username);
            AUDIT_LOGGER.warn("LoginFailure user={}, reason=invalid_credentials", username);
            return Map.of(
                    "status", "failure",
                    "message", "Ungültige Anmeldedaten"
            );
        }
    }

    /**
     * Beendet die aktuelle Benutzersitzung und löscht den SecurityContext.
     *
     * HTTP-Methode: POST
     * URL: /api/logout
     *
     * @param request Das HttpServletRequest, um die aktuelle Session zu invalidieren.
     * @return Eine Map, die den erfolgreichen Logout-Vorgang bestätigt.
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        // Audit: Logout
        String userName = (SecurityContextHolder.getContext().getAuthentication() != null)
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "unknown";
        AUDIT_LOGGER.info("Logout user={}, ip={}", userName, request.getRemoteAddr());

        return Map.of(
                "status", "success",
                "message", "Logout erfolgreich. Session ungültig."
        );
    }

    /**
     * Ruft den aktuellen CSRF-Token ab, der in der aktuellen Anfrage hinterlegt ist.
     *
     * HTTP-Methode: GET
     * URL: /api/csrf-token
     *
     * @param request Das HttpServletRequest, aus dem der CSRF-Token extrahiert wird.
     * @return Eine Map, die den CSRF-Token enthält.
     */
    @GetMapping("/csrf-token")
    public Map<String, String> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            return Map.of("csrfToken", csrfToken.getToken());
        } else {
            return Map.of("csrfToken", "");
        }
    }

    // Private Hilfsmethoden zur Handhabung fehlgeschlagener Loginversuche und zur Rollenbestimmung

    private boolean isUserBlocked(String username) {
        String key = username.toLowerCase();
        List<Long> timestamps = failedLoginsMap.get(key);
        if (timestamps == null) {
            return false;
        }
        cleanupOldTimestamps(timestamps);
        return timestamps.size() >= MAX_LOGIN_FAILURES;
    }

    private void recordFailedLogin(String username) {
        String key = username.toLowerCase();
        failedLoginsMap.putIfAbsent(key, new ArrayList<>());
        List<Long> timestamps = failedLoginsMap.get(key);

        cleanupOldTimestamps(timestamps);
        timestamps.add(Instant.now().toEpochMilli());
    }

    private void resetFailedLogins(String username) {
        failedLoginsMap.remove(username.toLowerCase());
    }

    private void cleanupOldTimestamps(List<Long> timestamps) {
        long now = Instant.now().toEpochMilli();
        timestamps.removeIf(ts -> (now - ts) > TIME_WINDOW_MILLIS);
    }

    private String determineUserRole(String username) {
        String resolvedRole = "ROLE_STUDENT";
        Optional<at.ac.tgm.ad.entry.UserEntry> userOpt = userService.findBysAMAccountName(username);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            if (adminListService.isAdmin(user.getSAMAccountName())) {
                resolvedRole = "ROLE_ADMIN";
            } else {
                String mail = user.getMail();
                if (mail != null && mail.toLowerCase().contains("student")) {
                    resolvedRole = "ROLE_STUDENT";
                } else {
                    resolvedRole = "ROLE_TEACHER";
                }
            }
        }
        return resolvedRole;
    }
}
