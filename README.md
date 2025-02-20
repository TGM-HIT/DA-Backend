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
ADMINS=mmustermann,emustermann
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

- [Eingeloggter User](http://localhost:8080/)
- [HIT-Schueler auflisten](http://localhost:8080/list/schueler)
- [Lehrer auflisten](http://localhost:8080/list/lehrer)
- [Person anhand des Nachnamen suchen](http://localhost:8080/find/Pointner)