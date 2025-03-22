package at.ac.tgm.controller;

import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.ad.util.EntryBase;
import at.ac.tgm.api.ADLDAPDemoApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Name;
import java.util.List;

@RestController
@Slf4j
public class ADLDAPDemoController implements ADLDAPDemoApi {
    @Autowired
    private UserService userService;
    
    @Override
    public Authentication getAuthCurrentUser(Authentication authentication) {
        return authentication;
    }
    
    @Override
    public List<String> listLehrer() {
        return userService.listUserCNs(EntryBase.LEHRER);
    }
    
    @Override
    public ResponseEntity<UserEntry> findUser(@PathVariable("surname") String surname) {
        return ResponseEntity.of(userService.findBySurname(surname, true));
    }
    
    @Override
    public List<Name> entities() {
        return userService.listUserDNs(EntryBase.SCHUELER_HIT);
    }
    
}
