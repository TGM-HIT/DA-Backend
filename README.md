# DA-Backend

Gemeinsames Spring Boot Backend für Diplomarbeitsprojekte.

## Frontend

Für eine Beispielimplementierung eines Frontends siehe [DA Frontend Vue Template](https://github.com/TGM-HIT/DA-Frontend-Vue-Template).

## Tickets

Neben den Issues auf Github, gibt es auch ein [Kanban-Board](https://projekte.tgm.ac.at/youtrack/agiles/145-117/current), das geplante/in Arbeit befindliche Features/Bugs enthält (allgemein, nicht auf ein DA-Projekt bezogen).

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

## API-Dokumentation

Siehe `/swagger-ui/index.html` für eine Dokumentation der Endpunkte.
Login/Logout unter `/auth/login` (siehe Beispiel in der Swagger-Ui) bzw. `/auth/logout`.

## Modul für ein neues DA-Projekt anlegen

1. Einen Projekt-main mit der Kurzbezeichnung des DA-Themas vom main Branch erstellen
2. beispiel-Modul duplizieren und auf die Bezeichnung des DA-Themas umbenennen
3. Modul in der `settings.gradle` ergänzen
4. Modul in der `server/build.gradle` importieren

## Review Prozess, Merging und CI/CD

1. Lösen Sie Ihre Features in eigenen Branches, welche das Projektkürzel enthalten, z.B.: `beispiel/ampeln-eintragen`.
2. Mergen Sie Ihre Features in den Projekt-main-Branch (bestenfalls nachdem ein Teammitglied die Änderungen des Feature-Branch reviewed hat). Halten Sie den Projekt-main-Branch in regelmäßig aktuell mit dem main-branch (durch Rebase/Reinmergen).
3. Bei Meilensteinen (abgeschlossenen Feature-Umfang Frontend&Backend) erstellen Sie einen Pull Request auf den main und weisen Sie Ihren DA-Betreuer als Assignee zu.
4. Betreuer approved den Pull Request oder verlangt Änderungen. Die Verantwortung der Betreuer ist hierbei auf Konflikte zwischen verschiedenen DA-Projekten zu achten. Also insbesondere:
   * Keine Namensüberschneidungen bei gemeinsam genutzten Resourcen (Tabellen, Request-Pfade, LDAP-Repository)
   * Konfigurationen (application.properties, SecurityConfig)
5. Sollte Ihr Betreuer Änderungen verlangen, adressieren Sie diese folgendermaßen:
    * Verlangte Codeänderungen: Implementieren Sie diese Änderungen und resolven Sie die Discussion, sobald der Fix gepushed ist.
    * Fragen: Antworten Sie auf die Discussion, der Betreuer resolved die Discussion, sobald die Frage geklärt ist.
6. Sie können den Pull Request selbst mergen, sobald alle Checks erfüllt sind (Betreuer-Approval vorhanden, `gradle build`-Action läuft durch, keine Konflikte mit main). Nach dem Merge läuft automatisch eine Deployment-Action, welche ein Docker Image baut und auf Dockerhub hochlädt. Noch ausständig: Der Produktion-Server pulled in regelmäßigen Abständen die neueste Version des Docker Image und restartet den Server.

