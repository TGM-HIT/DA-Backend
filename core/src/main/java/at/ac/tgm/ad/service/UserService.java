package at.ac.tgm.ad.service;

import at.ac.tgm.ad.entry.GroupEntry;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.repository.GroupRepository;
import at.ac.tgm.ad.repository.UserRepository;
import at.ac.tgm.ad.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    
    public Optional<UserEntry> findByMail(String email) {
        Optional<UserEntry> user = userRepository.findByMail(email);
        return user;
    }
    public Optional<UserEntry> findBysAMAccountName(String sAMAccountName) {
        Optional<UserEntry> user = userRepository.findBysAMAccountName(sAMAccountName);
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
}
