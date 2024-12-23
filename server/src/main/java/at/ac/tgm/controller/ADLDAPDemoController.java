package at.ac.tgm.controller;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.ad.util.EntryBase;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Name;
import java.util.List;

@RestController
public class ADLDAPDemoController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping({"", "/"})
    public Authentication getAuthCurrentUser(HttpSession session) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth;
    }
    
    @Secured(Roles.LEHRER)
    @GetMapping("/list/lehrer")
    public List<String> listLehrer() {
        return userService.listUserCNs(EntryBase.LEHRER);
    }
    
    @GetMapping("/find/{surname}")
    public ResponseEntity<UserEntry> findUser(@PathVariable("surname") String surname) {
        return ResponseEntity.of(userService.findBySurname(surname, true));
    }
    
    @Secured({Roles.SCHUELER, Roles.LEHRER})
    @GetMapping("/list/schueler")
    public List<Name> entities() {
        return userService.listUserDNs(EntryBase.SCHUELER_HIT);
    }
    
}
