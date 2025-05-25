package at.ac.tgm.service;

import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.entity.Betreuer;
import at.ac.tgm.exception.ResourceNotFoundException;
import at.ac.tgm.repository.BetreuerRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BetreuerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BetreuerService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private BetreuerRepository betreuerRepository;

    /**
     * Exportiert die Betreuerliste als CSV-Datei.
     *
     * @param search Optionaler Suchbegriff.
     * @param sortField Feld, nach dem sortiert werden soll.
     * @param sortDirection Sortierrichtung ("asc" oder "desc").
     * @param statusFilter Optionaler Statusfilter.
     * @return Byte-Array mit CSV-Inhalt.
     * @throws IOException Falls ein Fehler beim Schreiben auftritt.
     */
    public byte[] exportBetreuerListAsCsv(String search, String sortField, String sortDirection, String statusFilter) throws IOException {
        LOGGER.debug("Exportiere Betreuerliste als CSV");
        List<Betreuer> betreuerList = searchBetreuer(search, sortField, sortDirection, statusFilter);
        StringBuilder sb = new StringBuilder();
        sb.append("ID,SamAccountName,Vorname,Nachname,Email,Status,MaxProjekte,VergebeneProjekte,FreieProjekte,DisplayName\n");
        for (Betreuer b : betreuerList) {
            sb.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%d,%s\n",
                    b.getId(),
                    escapeCsv(b.getSamAccountName()),
                    escapeCsv(b.getVorname()),
                    escapeCsv(b.getNachname()),
                    escapeCsv(b.getEmail()),
                    escapeCsv(b.getStatus()),
                    b.getMaxProjekte() != null ? b.getMaxProjekte() : "",
                    b.getVergebeneProjekte() != null ? b.getVergebeneProjekte() : "0",
                    b.getFreieProjekte(),
                    escapeCsv(b.getDisplayName())
            ));
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Exportiert die Betreuerliste als Excel-Datei.
     *
     * @param search Optionaler Suchbegriff.
     * @param sortField Feld, nach dem sortiert werden soll.
     * @param sortDirection Sortierrichtung ("asc" oder "desc").
     * @param statusFilter Optionaler Statusfilter.
     * @return Byte-Array mit Excel-Inhalt.
     * @throws IOException Falls ein Fehler beim Erstellen oder Schreiben auftritt.
     */
    public byte[] exportBetreuerListAsExcel(String search, String sortField, String sortDirection, String statusFilter) throws IOException {
        LOGGER.debug("Exportiere Betreuerliste als Excel");
        List<Betreuer> betreuerList = searchBetreuer(search, sortField, sortDirection, statusFilter);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Betreuer");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("SamAccountName");
        header.createCell(2).setCellValue("Vorname");
        header.createCell(3).setCellValue("Nachname");
        header.createCell(4).setCellValue("Email");
        header.createCell(5).setCellValue("Status");
        header.createCell(6).setCellValue("MaxProjekte");
        header.createCell(7).setCellValue("VergebeneProjekte");
        header.createCell(8).setCellValue("FreieProjekte");
        header.createCell(9).setCellValue("DisplayName");

        int rowNum = 1;
        for (Betreuer b : betreuerList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(b.getId());
            row.createCell(1).setCellValue(b.getSamAccountName());
            row.createCell(2).setCellValue(b.getVorname());
            row.createCell(3).setCellValue(b.getNachname());
            row.createCell(4).setCellValue(b.getEmail());
            row.createCell(5).setCellValue(b.getStatus());
            row.createCell(6).setCellValue(b.getMaxProjekte() != null ? b.getMaxProjekte() : 0);
            row.createCell(7).setCellValue(b.getVergebeneProjekte() != null ? b.getVergebeneProjekte() : 0);
            row.createCell(8).setCellValue(b.getFreieProjekte());
            row.createCell(9).setCellValue(b.getDisplayName());
        }

        for (int i = 0; i <= 9; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    /**
     * Exportiert die Betreuerliste als PDF-Datei.
     *
     * @param search Optionaler Suchbegriff.
     * @param sortField Feld, nach dem sortiert werden soll.
     * @param sortDirection Sortierrichtung ("asc" oder "desc").
     * @param statusFilter Optionaler Statusfilter.
     * @return Byte-Array mit PDF-Inhalt.
     * @throws IOException Falls ein Fehler beim Erstellen oder Schreiben auftritt.
     */
    public byte[] exportBetreuerListAsPdf(String search, String sortField, String sortDirection, String statusFilter) throws IOException {
        LOGGER.debug("Exportiere Betreuerliste als PDF");
        List<Betreuer> betreuerList = searchBetreuer(search, sortField, sortDirection, statusFilter);
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        float margin = 50;
        float yStart = PDRectangle.LETTER.getHeight() - margin;
        float rowHeight = 20;
        int cols = 10;
        float[] colWidths = {40, 80, 60, 60, 100, 60, 60, 60, 60, 100};
        float tableYPosition = yStart - rowHeight;

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        float xPosition = margin;
        String[] headers = {"ID", "SamAccountName", "Vorname", "Nachname", "Email", "Status", "MaxProj", "VergProj", "FreieProj", "DisplayName"};
        for (int i = 0; i < cols; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(xPosition + 2, tableYPosition - 15);
            contentStream.showText(headers[i]);
            contentStream.endText();
            xPosition += colWidths[i];
        }

        contentStream.setFont(PDType1Font.HELVETICA, 10);
        tableYPosition -= rowHeight;
        for (Betreuer b : betreuerList) {
            xPosition = margin;
            String[] data = {
                    b.getId().toString(),
                    b.getSamAccountName(),
                    b.getVorname(),
                    b.getNachname(),
                    b.getEmail(),
                    b.getStatus(),
                    b.getMaxProjekte() != null ? b.getMaxProjekte().toString() : "",
                    b.getVergebeneProjekte() != null ? b.getVergebeneProjekte().toString() : "0",
                    String.valueOf(b.getFreieProjekte()),
                    b.getDisplayName() != null ? b.getDisplayName() : ""
            };
            for (int i = 0; i < cols; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition + 2, tableYPosition - 15);
                String text = data[i] != null ? data[i] : "";
                if (text.length() > 20) {
                    text = text.substring(0, 17) + "...";
                }
                contentStream.showText(text);
                contentStream.endText();
                xPosition += colWidths[i];
            }
            tableYPosition -= rowHeight;
            if (tableYPosition < margin) {
                contentStream.close();
                page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                tableYPosition = yStart - rowHeight;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                xPosition = margin;
                for (int i = 0; i < cols; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition + 2, tableYPosition - 15);
                    contentStream.showText(headers[i]);
                    contentStream.endText();
                    xPosition += colWidths[i];
                }
                tableYPosition -= rowHeight;
                contentStream.setFont(PDType1Font.HELVETICA, 10);
            }
        }
        contentStream.close();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();
        return baos.toByteArray();
    }

    /**
     * Escaped einen String für die CSV-Ausgabe, indem Sonderzeichen wie Kommas und Anführungszeichen behandelt werden.
     *
     * @param input Der zu escapende String.
     * @return Der angepasste String.
     */
    private String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            input = input.replace("\"", "\"\"");
            return "\"" + input + "\"";
        }
        return input;
    }

    /**
     * Aktualisiert die Betreuerliste aus dem LDAP.
     * Dabei wird nun auch ein Lehrer automatisch als Betreuer angelegt, falls er in LDAP vorhanden ist.
     */
    public void refreshBetreuerListFromLDAP() {
        LOGGER.info("Aktualisiere Betreuerliste aus LDAP ...");
        List<String> allSAMs = userService.collectAllSAMAccountNamesPaged();
        for (String sam : allSAMs) {
            // Auto-add teacher if not existing
            Betreuer b = betreuerRepository.findBySamAccountNameIgnoreCase(sam)
                    .orElseGet(() -> findeOderErstelleBetreuer(sam));
            userService.findBysAMAccountNameWithGroups(sam).ifPresent(user -> {
                b.setVorname(user.getGivenName());
                b.setNachname(user.getSn());
                b.setEmail(user.getMail());
                b.setDisplayName(user.getDisplayName());
                if (b.getStatus() == null) {
                    b.setStatus("verfügbar");
                }
                betreuerRepository.save(b);
            });
        }
        LOGGER.info("Betreuer-Refresh abgeschlossen. Anzahl der aktualisierten Einträge: {}", allSAMs.size());
    }

    /**
     * Sucht einen bestehenden Betreuer anhand des sAMAccountNames oder legt diesen automatisch neu an,
     * falls kein Eintrag existiert.
     *
     * @param samAccountName Der sAMAccountName des Betreuers.
     * @return Der vorhandene oder neu erstellte Betreuer.
     * @throws ResourceNotFoundException Falls der LDAP-Benutzer nicht gefunden wird.
     */
    public Betreuer findeOderErstelleBetreuer(String samAccountName) {
        Optional<Betreuer> existing = betreuerRepository.findBySamAccountNameIgnoreCase(samAccountName);
        if (existing.isPresent()) {
            return existing.get();
        }
        return userService.findBysAMAccountName(samAccountName)
                .map(ldapUser -> {
                    Betreuer neuerBetreuer = new Betreuer();
                    neuerBetreuer.setSamAccountName(samAccountName);
                    neuerBetreuer.setVorname(ldapUser.getGivenName());
                    neuerBetreuer.setNachname(ldapUser.getSn());
                    neuerBetreuer.setEmail(ldapUser.getMail());
                    neuerBetreuer.setDisplayName(ldapUser.getDisplayName());
                    neuerBetreuer.setStatus("verfügbar");
                    neuerBetreuer.setMaxProjekte(3);
                    neuerBetreuer.setVergebeneProjekte(0);
                    LOGGER.info("LDAP-Benutzer {} gefunden und als neuer Betreuer erstellt.", samAccountName);
                    return betreuerRepository.save(neuerBetreuer);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Lehrer mit sAMAccountName " + samAccountName + " wurde in LDAP nicht gefunden."));
    }

    public List<Betreuer> searchBetreuer(String search, String sortField, String sortDirection, String statusFilter) {
        List<Betreuer> all = betreuerRepository.findAll();
        all = all.stream().filter(b -> b.getMaxProjekte() != null).collect(Collectors.toList());
        if (statusFilter != null && !statusFilter.isBlank()) {
            all = all.stream()
                    .filter(b -> b.getStatus() != null && b.getStatus().equalsIgnoreCase(statusFilter))
                    .collect(Collectors.toList());
        }
        if (search != null && !search.isEmpty()) {
            String lower = search.toLowerCase();
            all = all.stream().filter(b ->
                    (b.getVorname() != null && b.getVorname().toLowerCase().contains(lower)) ||
                            (b.getNachname() != null && b.getNachname().toLowerCase().contains(lower)) ||
                            (b.getEmail() != null && b.getEmail().toLowerCase().contains(lower)) ||
                            (b.getSamAccountName() != null && b.getSamAccountName().toLowerCase().contains(lower))
            ).collect(Collectors.toList());
        }
        Comparator<Betreuer> comparator = Comparator.comparing(Betreuer::getId);
        if ("vorname".equalsIgnoreCase(sortField)) {
            comparator = Comparator.comparing(Betreuer::getVorname, String.CASE_INSENSITIVE_ORDER);
        } else if ("nachname".equalsIgnoreCase(sortField)) {
            comparator = Comparator.comparing(Betreuer::getNachname, String.CASE_INSENSITIVE_ORDER);
        } else if ("email".equalsIgnoreCase(sortField)) {
            comparator = Comparator.comparing(Betreuer::getEmail, String.CASE_INSENSITIVE_ORDER);
        } else if ("status".equalsIgnoreCase(sortField)) {
            comparator = Comparator.comparing(Betreuer::getStatus, String.CASE_INSENSITIVE_ORDER);
        }
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        all.sort(comparator);
        return all;
    }

    /**
     * Aktualisiert den Status eines Betreuers anhand des sAMAccountNames.
     * Hier wird ausschließlich geprüft, ob der Benutzer in mindestens einer LDAP-Gruppe (über den cn)
     * sowohl die Ziffer "5", das Wort "hit" UND "lehrer" enthält.
     * Lehrer, die über preauthorize identifiziert wurden, dürfen nur ihren eigenen Status ändern, während Admins beliebige Namen eingeben können.
     *
     * @param samAccountName Der sAMAccountName des Betreuers.
     * @param newStatus Der neue Status.
     * @return Der aktualisierte Betreuer.
     */
    public Betreuer updateBetreuerStatusBySam(String samAccountName, String newStatus) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();
        // Unabhängig vom Adminstatus: Überprüfe, ob der Benutzer in mindestens einer Gruppe (cn) "5", "hit" und "lehrer" enthält.
        if (!isTeacherEligible(samAccountName)) {
            throw new SecurityException("Nur Lehrer der 5. Klasse der HIT Abteilung dürfen als Betreuer geführt werden.");
        }
        // Falls kein Admin: nur der eigene Status darf geändert werden.
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !currentUser.equalsIgnoreCase(samAccountName)) {
            throw new SecurityException("Lehrer dürfen nur ihren eigenen Status ändern.");
        }
        Betreuer betreuer = betreuerRepository.findBySamAccountNameIgnoreCase(samAccountName)
                .orElseGet(() -> findeOderErstelleBetreuer(samAccountName));
        LOGGER.info("Aktualisiere Betreuer-Status: samAccountName={}, alter Status={}, neuer Status={}",
                samAccountName, betreuer.getStatus(), newStatus);
        betreuer.setStatus(newStatus);
        return betreuerRepository.save(betreuer);
    }

    /**
     * Aktualisiert die maximale Kapazität eines Betreuers anhand des sAMAccountNames.
     * Hier wird ausschließlich geprüft, ob der Benutzer in mindestens einer LDAP-Gruppe (über den cn)
     * sowohl "5", "hit" und "lehrer" enthält.
     * Lehrer, die über preauthorize identifiziert wurden, dürfen nur ihre eigene Kapazität ändern, während Admins beliebige Namen eingeben können.
     *
     * @param samAccountName Der sAMAccountName des Betreuers.
     * @param maxProjekte Die neue maximale Anzahl betreuter Projekte.
     * @return Der aktualisierte Betreuer.
     */
    public Betreuer updateBetreuerCapacityBySam(String samAccountName, int maxProjekte) {
        if (maxProjekte < 0 || maxProjekte > 3) {
            throw new IllegalArgumentException("Die Kapazität muss zwischen 0 und 3 liegen!");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();
        // Unabhängig vom Adminstatus: Überprüfe, ob der Benutzer in mindestens einer Gruppe (cn) "5", "hit" und "lehrer" enthält.
        if (!isTeacherEligible(samAccountName)) {
            throw new SecurityException("Nur Lehrer der 5. Klasse der HIT Abteilung dürfen als Betreuer geführt werden.");
        }
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !currentUser.equalsIgnoreCase(samAccountName)) {
            throw new SecurityException("Lehrer dürfen nur ihre eigene Kapazität ändern.");
        }
        Betreuer betreuer = betreuerRepository.findBySamAccountNameIgnoreCase(samAccountName)
                .orElseGet(() -> findeOderErstelleBetreuer(samAccountName));
        int currentAssigned = betreuer.getVergebeneProjekte() != null ? betreuer.getVergebeneProjekte() : 0;
        if (currentAssigned > maxProjekte) {
            throw new IllegalArgumentException("Neue Kapazität kann nicht kleiner sein als die bereits vergebenen Projekte (" + currentAssigned + ")");
        }
        betreuer.setMaxProjekte(maxProjekte);
        return betreuerRepository.save(betreuer);
    }

    /**
     * Prüft, ob der Benutzer (basierend auf seinem sAMAccountName) in mindestens einer LDAP-Gruppe
     * (über den cn) enthalten ist, die case-insensitiv die Ziffer "5", das Wort "hit" UND "lehrer" enthält.
     * Ist dies der Fall, gilt der Benutzer als berechtigt, als Betreuer geführt zu werden.
     *
     * @param samAccountName Der sAMAccountName des Benutzers.
     * @return true, wenn mindestens eine Gruppe alle drei Kriterien erfüllt, ansonsten false.
     * @throws ResourceNotFoundException Falls der LDAP-Benutzer nicht gefunden wird.
     */
    private boolean isTeacherEligible(String samAccountName) {
        Optional<at.ac.tgm.ad.entry.UserEntry> userOpt = userService.findBysAMAccountNameWithGroups(samAccountName);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("LDAP-User nicht gefunden: " + samAccountName);
        }
        var user = userOpt.get();
        if (user.getGroups() == null || user.getGroups().isEmpty()) {
            return false;
        }
        return user.getGroups().stream().anyMatch(g ->
                g.getCn() != null &&
                        g.getCn().toLowerCase().contains("5") &&
                        g.getCn().toLowerCase().contains("hit") &&
                        g.getCn().toLowerCase().contains("lehrer")
        );
    }
}
