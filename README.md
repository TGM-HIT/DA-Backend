# DA-Backend
Gemeinsames Spring Boot Backend für Diplomarbeitsprojekte

## Voraussetzungen

Getestet mit:

- SDK Eclipse Temurin (AdoptOpenJDK HotSpot) 21.0.4
- Gradle 8.10.1

Für die Active Directory LDAP Anbindung muss auf der Root eine `.env` Datei angelegt werden, welche Folgendes enthält:

```
AD_USER=insertTGMEmailAdressHere
AD_PASSWORD=insertTGMPasswortHere
```

Im Ordner `server/src/main/resources/certs/` RSA-Keys erstellen wie unter [JWT Private Key](https://bootify.io/spring-security/signing-jwts-with-private-key-spring-security.html) beschrieben:

```cmd
# create key pair
openssl genrsa -out keypair.pem 2048

# extract public key
openssl rsa -in keypair.pem -pubout -out public.pem

# extract private key
openssl pkcs8 -in keypair.pem -topk8 -nocrypt -inform PEM -outform PEM -out private.pem
```

## Gradle Projektstruktur

Die angedachte Struktur ist folgende:

- Im **server** Modul wird 
  - der Tomcat gestartet
  - im SecurityConfig die Pfade konfiguriert, bei denen ein Login notwendig ist.
  - alle enthalten Projekte (und core) im `implementation project(':beispielprojekt')` importiert
- Das **beispiel** -Modul ist eine Vorlage für ein Modul, wo eine Diplomarbeit ihren spezifischen Code entwickeln soll.
- **core**: Alle Module importieren dies und hier soll auch der von mehreren Projekten genutzte Code wie z.B. Active Directory LDAP Anbindung reinkommen

## Beispiel-API-Endpoints

### Active Directory LDAP Anbindung

- [Eingeloggter User](http://localhost:8080/beispielprojekt)
- [HIT-Schueler auflisten](http://localhost:8080/beispielprojekt/list/schueler)
- [Lehrer auflisten](http://localhost:8080/beispielprojekt/list/lehrer)
- [Person anhand des Nachnamen suchen](http://localhost:8080/beispielprojekt/find/Pointner)