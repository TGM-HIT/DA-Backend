# DA-Backend

Gemeinsames Spring Boot Backend für Diplomarbeitsprojekte.

## Frontend

Für eine Beispielimplementierung eines Frontends siehe [DA Frontend Vue Template](https://github.com/TGM-HIT/DA-Frontend-Vue-Template).

## Tickets

Neben den Issues auf Github, gibt es auch ein [Kanban-Board](https://projekte.tgm.ac.at/youtrack/agiles/145-117/current), das geplante/in Arbeit befindliche Features/Bugs enthält (allgemein, nicht auf ein DA-Projekt bezogen).

## Voraussetzungen

### Getestet mit:

- SDK Eclipse Temurin (AdoptOpenJDK HotSpot) 21.0.4
- Gradle 8.10.1

### Environment-Variablen:

Für die Active Directory LDAP Anbindung muss auf der Root eine `.env` Datei angelegt werden, welche die Werte in der [.env.example](.env.example) enthält.

## Gradle Projektstruktur

Die angedachte Struktur ist folgende:

- Im **server**-Modul wird
    - der Tomcat gestartet
    - im SecurityConfig die Pfade konfiguriert, bei denen ein Login notwendig ist.
    - alle enthalten Projekte (und core) im `implementation project(':beispielprojekt')` importiert
- Das **beispiel**-Modul ist eine Vorlage für ein Modul, wo eine Diplomarbeit ihren spezifischen Code entwickeln soll.
- **core**: Alle Module importieren dies und hier soll auch der von mehreren Projekten genutzte Code wie z.B. Active Directory LDAP Anbindung reinkommen

## API-Dokumentation

Siehe [/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) für eine Dokumentation der Endpunkte.

### CSRF

Der Cross-Site-Request-Forgery (CSRF) Token wird bei jeder Response als Cookie (XSRF-TOKEN) mitgeschickt und bei modifizierenden Requests (z.B.: POST) im Header (als X-XSRF-TOKEN) mitgeschickt werden.
Viele JS-Request-Bibliotheken geht diese automatisch, z.B.:

```ts
axios.defaults.withXSRFToken = true
axios.defaults.xsrfCookieName = "XSRF-TOKEN"
axios.defaults.xsrfHeaderName = "X-XSRF-TOKEN"
```

### Authentifizierung

Login/Logout unter [/auth/login](http://localhost:8080/swagger-ui/index.html#/authentication-controller/authenticateUser) (siehe Beispiel in der Swagger-Ui) bzw. [/auth/logout](http://localhost:8080/swagger-ui/index.html#/authentication-controller/logout).
Ausnahmen für Pfade, die ohne Login erreichbar sein sollen, können in der [SecurityConfig](server/src/main/java/at/ac/tgm/config/SecurityConfig.java) -> securityFilterChain ausgenommen werden.

### Simulate User

Login mit einem anderen Account ist möglich, indem die Anwendung mit aktivem Profil "dev" gestartet wird (In IntelliJ: Application -> Edit Configuration -> Actives Profiles -> "dev" hinzufügen).
Damit kann zum Beispiel die Zugriffsrechte einer anderen Rolle getestet werden.
Aufruf mit `simulate = true`, Password ist egal.

### Autorisierung

```java
@Secured(Roles.TEACHER)
@GetMapping
public ResponseEntity<List<ItemDto>> getAllItems()
```

### CORS

Erlaubte Request-Domains können in der [SecurityConfig](server/src/main/java/at/ac/tgm/config/SecurityConfig.java) -> corsConfigurer ergänzt werden.

## Modul für ein neues DA-Projekt anlegen

1. Einen Projekt-main mit der Kurzbezeichnung des DA-Themas vom main Branch erstellen
2. beispiel-Modul duplizieren und auf die Bezeichnung des DA-Themas umbenennen
3. Modul in der [settings.gradle](settings.gradle) ergänzen
4. Modul in der [server/build.gradle](server/build.gradle) importieren

## Review / Merging

1. Lösen Sie Ihre Features in eigenen Branches, welche das Projektkürzel enthalten, z.B.: `beispiel/ampeln-eintragen`.
2. Mergen Sie Ihre Features in den Projekt-Branch (z.B.: `beispiel`), bestenfalls nachdem ein Teammitglied die Änderungen des Feature-Branch reviewed hat. Halten Sie den Projekt-Branch in regelmäßig aktuell mit dem main-branch (durch *Rebase beispiel onto main* / *Merge main into beispiel*).
3. Bei Meilensteinen (abgeschlossenen Funktionsumfang Frontend & Backend) erstellen Sie einen Pull Request auf den main, weisen Sie Ihren DA-Betreuer als Assignee zu und fordern Sie einen Review an.
4. Betreuer genehmigt (approved) den Pull Request oder verlangt Änderungen (siehe *Genehmigung Betreuer* und *Diskussionen lösen*).
5. Sie können den Pull Request selbst mergen, sobald alle Checks erfüllt sind (*Betreuer-Genehmigung vorhanden*, *action läuft durch*, *alle Diskussionen gelöst* und *keine Merge-Konflikte mit main*)

### Genehmigung Betreuer

Die Verantwortung der Betreuer ist auf Konflikte zwischen verschiedenen DA-Projekten zu achten, insbesondere:

* Keine Namensüberschneidungen bei gemeinsam genutzten Resourcen (Tabellen, Request-Pfade, LDAP-Repository)
* Konfigurationen (application.properties, SecurityConfig) sind allgemein genug gehalten und mit allen Projekte kompatibel.

Dazu reicht es sich die Änderungen im **server** und **core** genauer anzuschauen, im Projekt-Modul reicht ein Überfliegen und z.B.: auf kollisionsfreie Tabellenbenennung (`@Table(name = Consts.BEISPIEL_TABLE_PREFIX + "ITEM")`) oder Pfadbenennung (`@RequestMapping(Consts.BEISPIEL_PATH_PREFIX + "/item")`) zu achten.

Als alternative Ansprechperson für Reviews/Approvals steht Prof. Michael Pointner (Account: mpointner) zur Verfügung.

### Diskussionen lösen 

Sollte Ihr Betreuer Änderungen verlangen, adressieren Sie diese folgendermaßen:

* **Verlangte Codeänderungen:** Implementieren Sie diese Änderungen und lösen Sie die Discussion selbst, sobald der Fix gepushed ist.
* **Fragen:** Antworten Sie auf die Discussion, der Betreuer resolved die Discussion, sobald die Frage ausreichend geklärt ist.

Nochdem Sie alle Diskussionen reagiert haben, fordern Sie erneut einen Review von Ihrem Betreuer an.

### CI/CD

* **Continuous Integration (CI):** Sobald ein Pull Request auf den main erstellt wurde, läuft sofort (und bei jedem zukünftigen Push eines neuen Commits) eine [Integration-Action](.github/gradle-build.yml), welche Ihren Code mit `gradle build` auf Kompilierbarkeit des Codes und auf Durchlaufen der Tests überprüft. Dies stell sicher, dass der main-Branch zu jeder Zeit kompilierbar ist.
* **Continous Delivery (CD):** Nach dem Merge läuft automatisch eine [Deployment-Action](.github/docker-build-push.yml), welche ein Docker Image (siehe [Dockerfile](Dockerfile)) baut und auf [Dockerhub](https://hub.docker.com/repository/docker/mpointner/da-backend/general) hochlädt. Noch ausständig: Der Produktion-Server lädt in regelmäßigen Abständen die neueste Version des Docker Image herunter und startet im Fall einer neuen Version des Image den Server neu (`docker-compose pull; docker-compose up -d`).
