package at.ac.tgm.diplomarbeit.diplomdb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

import java.util.Collections;

/**
 * Konfigurationsklasse für die Active Directory LDAP-Authentifizierung.
 * Diese Klasse richtet den LDAP-Authentifizierungsprovider basierend auf den
 * in der Konfiguration hinterlegten Werten ein und stellt einen AuthenticationManager
 * zur Verfügung, der diesen Provider nutzt.
 */
//@Configuration
//EnableLdapRepositories
public class AdLdapConfig {

    /**
     * URL des LDAP-Servers.
     */
    @Value("${spring.ldap.urls}")
    private String url;

    /**
     * LDAP-Domain.
     */
    @Value("${spring.ldap.domain}")
    private String domain;

    /**
     * Konfiguriert den ActiveDirectoryLdapAuthenticationProvider.
     * Der Provider wird so eingestellt, dass er Fehlercodes in Exceptions umwandelt und
     * die übergebenen Anmeldedaten verwendet. Der Suchfilter ermittelt Benutzer anhand
     * ihres sAMAccountName.
     *
     * @return der konfigurierte ActiveDirectoryLdapAuthenticationProvider
     */
    @Bean
    ActiveDirectoryLdapAuthenticationProvider authenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider authenticationProvider =
                new ActiveDirectoryLdapAuthenticationProvider(domain, url);
        // Umwandlung von Sub-Errorcodes in Exceptions aktivieren
        authenticationProvider.setConvertSubErrorCodesToExceptions(true);
        // Authentifizierungsanfragedaten verwenden
        authenticationProvider.setUseAuthenticationRequestCredentials(true);
        // Suchfilter zur Ermittlung von Benutzern anhand des sAMAccountName setzen
        authenticationProvider.setSearchFilter("(sAMAccountName={1})");
        return authenticationProvider;
    }

    /**
     * Konfiguriert den AuthenticationManager unter Verwendung des LDAP-Providers.
     *
     * @param adProvider der ActiveDirectoryLdapAuthenticationProvider
     * @return der AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(ActiveDirectoryLdapAuthenticationProvider adProvider) {
        return new ProviderManager(Collections.singletonList(adProvider));
    }
}
