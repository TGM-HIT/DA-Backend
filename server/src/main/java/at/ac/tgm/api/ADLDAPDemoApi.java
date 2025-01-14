package at.ac.tgm.api;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.ad.entry.UserEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.naming.Name;
import java.util.List;

@RequestMapping("")
public interface ADLDAPDemoApi {
    @GetMapping({"", "/"})
    Authentication getAuthCurrentUser();
    
    @Secured(Roles.TEACHER)
    @GetMapping("/list/lehrer")
    List<String> listLehrer();
    
    @GetMapping("/find/{surname}")
    ResponseEntity<UserEntry> findUser(@PathVariable("surname") String surname);
    
    @Secured({Roles.STUDENT, Roles.TEACHER})
    @GetMapping("/list/schueler")
    List<Name> entities();
}
