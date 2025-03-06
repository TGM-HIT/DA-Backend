# DA-Backend

Gemeinsames Spring Boot Backend für Diplomarbeitsprojekte.

Für eine Beispielimplementierung eines Frontends siehe [DA Frontend Vue Template](https://github.com/TGM-HIT/DA-Frontend-Vue-Template).

## Voraussetzungen

Getestet mit:

- SDK Eclipse Temurin (AdoptOpenJDK HotSpot) 21.0.4
- Gradle 8.10.1

Für die Active Directory LDAP Anbindung muss auf der Root eine `.env` Datei angelegt werden, welche Folgendes enthält:

```
AD_USER=REPLACE_TGM_USER
AD_PASSWORD=REPLACE_TGM_PASSWORD
ADMINS=REPLACE_TGM_USER_1,REPLACE_TGM_USER_2
MARIADB_ROOT_PASSWORD=REPLACE_DATABASE_PASSWORD
MARIADB_DATABASE=REPLACE_DATABASE_NAME
MARIADB_PORT=3306
DOCKERHUB_USERNAME=REPLACE_YOUR_DOCKERHUB_USERNAME
DOCKERHUB_IMAGE=REPLACE_YOUR_DOCKERHUB_IMAGE
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