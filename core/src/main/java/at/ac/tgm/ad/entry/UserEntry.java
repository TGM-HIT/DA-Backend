package at.ac.tgm.ad.entry;

import at.ac.tgm.ad.util.EntryBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

import javax.naming.Name;
import java.util.List;
import java.util.Set;

@Entry(base = EntryBase.PEOPLE, objectClasses = {"user", "organizationalPerson", "person", "top"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntry {
    private @Id Name id;
    private @Attribute(name = "memberOf") Set<Name> memberOf;
    private @Transient Set<GroupEntry> groups;
    private @Attribute(name = "cn") String cn;
    private @Attribute(name = "sn") String sn;
    private @Attribute(name = "accountExpires") String accountExpires;
    private @Attribute(name = "badPasswordTime") String badPasswordTime;
    private @Attribute(name = "badPwdCount") String badPwdCount;
    private @Attribute(name = "codePage") String codePage;
    private @Attribute(name = "countryCode") String countryCode;
    private @Attribute(name = "department") String department; // Just Student, not Teachers
    private @Attribute(name = "displayName") String displayName;
    private @Attribute(name = "distinguishedName") String distinguishedName;
    private @Attribute(name = "dSCorePropagationData") List<String> dSCorePropagationData;
    private @Attribute(name = "employeeID") String employeeID; // Just Students
    private @Attribute(name = "employeeNumber") String employeeNumber; // Just Teachers
    private @Attribute(name = "employeeType") String employeeType;
    private @Attribute(name = "extensionAttribute2") String extensionAttribute2;
    private @Attribute(name = "extensionAttribute4") String extensionAttribute4;
    private @Attribute(name = "extensionAttribute5") String extensionAttribute5;
    private @Attribute(name = "givenName") String givenName;
    private @Attribute(name = "homeDirectory") String homeDirectory;
    private @Attribute(name = "homeDrive") String homeDrive;
    private @Attribute(name = "homeMDB") String homeMDB;
    private @Attribute(name = "info") String info; // Just Teachers
    private @Attribute(name = "instanceType") String instanceType;
    private @Attribute(name = "l") String l;
    private @Attribute(name = "lastLogoff") String lastLogoff;
    private @Attribute(name = "lastLogon") String lastLogon;
    private @Attribute(name = "lastLogonTimestamp") String lastLogonTimestamp;
    private @Attribute(name = "legacyExchangeDN") String legacyExchangeDN;
    private @Attribute(name = "lockoutTime") String lockoutTime; // Some Users
    private @Attribute(name = "logonCount") String logonCount;
    private @Attribute(name = "mail") String mail;
    private @Attribute(name = "mailNickname") String mailNickname;
    private @Attribute(name = "mDBUseDefaults") String mDBUseDefaults;
    private @Attribute(name = "mS-DS-ConsistencyGuid") String mSDSConsistencyGuid;
    private @Attribute(name = "msDS-ExternalDirectoryObjectId") String msDSExternalDirectoryObjectId;
    private @Attribute(name = "msDS-KeyCredentialLink") List<String> msDSKeyCredentialLink; // Just Students
    private @Attribute(name = "msExchArchiveQuota") String msExchArchiveQuota;
    private @Attribute(name = "msExchArchiveWarnQuota") String msExchArchiveWarnQuota;
    private @Attribute(name = "msExchCalendarLoggingQuota") String msExchCalendarLoggingQuota;
    private @Attribute(name = "msExchDumpsterQuota") String msExchDumpsterQuota;
    private @Attribute(name = "msExchDumpsterWarningQuota") String msExchDumpsterWarningQuota;
    private @Attribute(name = "msExchELCMailboxFlags") String msExchELCMailboxFlags;
    private @Attribute(name = "msExchHideFromAddressLists") String msExchHideFromAddressLists;
    private @Attribute(name = "msExchHomeServerName") String msExchHomeServerName;
    private @Attribute(name = "msExchMailboxGuid") String msExchMailboxGuid;
    private @Attribute(name = "msExchMailboxSecurityDescriptor") String msExchMailboxSecurityDescriptor;
    private @Attribute(name = "msExchMobileMailboxFlags") String msExchMobileMailboxFlags;
    private @Attribute(name = "msExchPoliciesIncluded") String msExchPoliciesIncluded;
    private @Attribute(name = "msExchRBACPolicyLink") String msExchRBACPolicyLink;
    private @Attribute(name = "msExchRecipientDisplayType") String msExchRecipientDisplayType;
    private @Attribute(name = "msExchRecipientTypeDetails") String msExchRecipientTypeDetails;
    private @Attribute(name = "msExchSafeSendersHash") String msExchSafeSendersHash;
    private @Attribute(name = "msExchSharingAnonymousIdentities") String msExchSharingAnonymousIdentities; // Just Teachers
    private @Attribute(name = "msExchTextMessagingState") List<String> msExchTextMessagingState;
    private @Attribute(name = "msExchUMDtmfMap") List<String> msExchUMDtmfMap;
    private @Attribute(name = "msExchUserAccountControl") String msExchUserAccountControl;
    private @Attribute(name = "msExchUserCulture") String msExchUserCulture;
    private @Attribute(name = "msExchVersion") String msExchVersion;
    private @Attribute(name = "msExchWhenMailboxCreated") String msExchWhenMailboxCreated;
    private @Attribute(name = "name") String name;
    private @Attribute(name = "objectCategory") String objectCategory;
    private @Attribute(name = "objectClass") List<String> objectClass;
    private @Attribute(name = "objectGUID") String objectGUID;
    private @Attribute(name = "objectSid") String objectSid;
    private @Attribute(name = "primaryGroupID") String primaryGroupID;
    private @Attribute(name = "profilePath") String profilePath;
    private @Attribute(name = "protocolSettings") String protocolSettings;
    private @Attribute(name = "proxyAddresses") List<String> proxyAddresses;
    private @Attribute(name = "pwdLastSet") String pwdLastSet;
    private @Attribute(name = "sAMAccountName") String sAMAccountName;
    private @Attribute(name = "sAMAccountType") String sAMAccountType;
    private @Attribute(name = "showInAddressBook") List<Name> showInAddressBook;
    private @Attribute(name = "userAccountControl") String userAccountControl;
    private @Attribute(name = "userPrincipalName") String userPrincipalName;
    private @Attribute(name = "uSNChanged") String uSNChanged;
    private @Attribute(name = "uSNCreated") String uSNCreated;
    
    @Override
    public String toString() {
        return "GroupEntry{" +
               "id=" + id +
               '}';
    }
}
