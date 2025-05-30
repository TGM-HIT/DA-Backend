package at.ac.tgm.ad.service;

import at.ac.tgm.ad.entry.GroupEntry;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.repository.GroupRepository;
import at.ac.tgm.ad.repository.UserRepository;
import at.ac.tgm.ad.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private LdapTemplate ldapTemplate;
    
    public Optional<UserEntry> findByCommonName(String cn, boolean loadGroups) {
        Optional<UserEntry> user = userRepository.findByCn(cn);
        if (loadGroups) {
            user.ifPresent(this::loadGroupMembers);
        }
        return user;
    }
    
    public Optional<UserEntry> findBySurname(String surname, boolean loadGroups) {
        Optional<UserEntry> user = userRepository.findBySn(surname);
        if (loadGroups) {
            user.ifPresent(this::loadGroupMembers);
        }
        return user;
    }
    
    public List<String> listUserCNs(String entryBase) {
        return ldapTemplate.list(entryBase);
    }
    
    public List<Name> listUserDNs(String entryBase) {
        Entry entryAnnotation = UserEntry.class.getDeclaredAnnotation(Entry.class);
        return ldapTemplate.search(LdapQueryBuilder.query().base(entryBase).where("objectclass").is(entryAnnotation.objectClasses()[0]),
                (AttributesMapper<Name>) attrs -> new LdapName((String) attrs.get("distinguishedName").get()));
    }
    
    private void loadGroupMembers(UserEntry lehrer) {
        Set<GroupEntry> groups =
                lehrer.getMemberOf().stream().map(member -> groupRepository.findByCn(Util.getCnFromName(member)).orElse(null)).filter(Objects::nonNull).collect(Collectors.toSet());
        lehrer.setGroups(groups);
    }

    public Optional<UserEntry> findByMail(String email) {
        Optional<UserEntry> user = userRepository.findByMail(email);
        return user;
    }
    public Optional<UserEntry> findBysAMAccountName(String sAMAccountName) {
        Optional<UserEntry> user = userRepository.findBysAMAccountName(sAMAccountName);
        return user;
    }
    
    /**
     * Neue öffentliche Methode, um einen Benutzer inkl. Gruppen zu laden.
     */
    public Optional<UserEntry> findBysAMAccountNameWithGroups(String sAMAccountName) {
        Optional<UserEntry> userOpt = findBysAMAccountName(sAMAccountName);
        if (userOpt.isPresent()) {
            loadGroupMembers(userOpt.get());
        }
        return userOpt;
    }
    
    /**
     * Sammelt alle sAMAccountNames von Usern aus dem LDAP, mithilfe der Paging-Methode.
     *
     * @return Eine Liste der gesammelten sAMAccountNames.
     */
    public List<String> collectAllSAMAccountNamesPaged() {
        return collectAllSAMAccountNamesPaged("(objectClass=user)");
    }
    
    /**
     * Sammelt alle sAMAccountNames aus dem LDAP, die dem angegebenen Filter entsprechen, mithilfe der Paging-Methode.
     *
     * @param filter Der LDAP-Filter, der angewendet werden soll.
     * @return Eine Liste der gesammelten sAMAccountNames.
     */
    public List<String> collectAllSAMAccountNamesPaged(String filter) {
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
                    this::extractSamAccountName,
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
}
