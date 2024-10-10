package at.ac.tgm.ad.repository;

import at.ac.tgm.ad.entry.GroupEntry;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends LdapRepository<GroupEntry> {
    Optional<GroupEntry> findByCn(String cn);
}
