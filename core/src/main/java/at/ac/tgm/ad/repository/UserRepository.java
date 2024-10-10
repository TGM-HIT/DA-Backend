package at.ac.tgm.ad.repository;

import at.ac.tgm.ad.entry.UserEntry;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends LdapRepository<UserEntry> {
    Optional<UserEntry> findByCn(String cn);
    Optional<UserEntry> findBySn(String sn);
    
    /*
    BUG: findAll() nicht nutzen, da das base-Attribute in der @Entry Annotation nicht beachtet wird und damit immer alle Nutzer auflistet werden:
    https://github.com/spring-projects/spring-data-ldap/issues/446
    Stattdessen sollte findAll(LdapQuery ldapQuery) genutzt werden
     */
}