package at.ac.tgm.ad.entry;

import at.ac.tgm.ad.util.EntryBase;
import lombok.*;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.List;
import java.util.Set;

@Entry(
        base = EntryBase.GROUP,
        objectClasses = { "group", "top" })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class GroupEntry {
    private @Id Name dn;
    private @Attribute(name = "cn") String cn;
    private @Attribute(name = "displayName") String displayName;
    private @Attribute(name = "distinguishedName") String distinguishedName;
    private @Attribute(name = "dSCorePropagationData") List<String> dSCorePropagationData;
    private @Attribute(name = "groupType") String groupType;
    private @Attribute(name = "instanceType") String instanceType;
    private @Attribute(name = "legacyExchangeDN") String legacyExchangeDN;
    private @Attribute(name = "mail") String mail;
    private @Attribute(name = "mailNickname") String mailNickname;
    private @Attribute(name = "member") Set<Name> member;
    private @Attribute(name = "memberOf") Name memberOf;
    private @Attribute(name = "msExchGroupExternalMemberCount") String msExchGroupExternalMemberCount;
    private @Attribute(name = "msExchGroupMemberCount") String msExchGroupMemberCount;
    private @Attribute(name = "msExchPoliciesIncluded") List<String> msExchPoliciesIncluded;
    private @Attribute(name = "msExchRecipientDisplayType") String msExchRecipientDisplayType;
    private @Attribute(name = "msExchRequireAuthToSendTo") String msExchRequireAuthToSendTo;
    private @Attribute(name = "msExchUMDtmfMap") List<String> msExchUMDtmfMap;
    private @Attribute(name = "msExchVersion") String msExchVersion;
    private @Attribute(name = "name") String name;
    private @Attribute(name = "objectCategory") String objectCategory;
    private @Attribute(name = "objectClass") List<String> objectClass;
    private @Attribute(name = "objectGUID") String objectGUID;
    private @Attribute(name = "objectSid") String objectSid;
    private @Attribute(name = "proxyAddresses") List<String> proxyAddresses;
    private @Attribute(name = "reportToOriginator") String reportToOriginator;
    private @Attribute(name = "sAMAccountName") String sAMAccountName;
    private @Attribute(name = "sAMAccountType") String sAMAccountType;
    private @Attribute(name = "showInAddressBook") List<String> showInAddressBook;
    private @Attribute(name = "uSNChanged") String uSNChanged;
    private @Attribute(name = "uSNCreated") String uSNCreated;
    private @Attribute(name = "whenChanged") String whenChanged;
    private @Attribute(name = "whenCreated") String whenCreated;
}
