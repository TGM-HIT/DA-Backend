package at.ac.tgm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception-Klasse, die eine spezifische Ausnahme signalisiert, wenn eine angeforderte Ressource
 * (z.B. ein Projekt, Dokument oder Benutzer) nicht gefunden werden kann.
 * Diese Ausnahme wird mit dem HTTP-Statuscode 404 (NOT_FOUND) versehen, um den entsprechenden Fehlerstatus
 * in der HTTP-Antwort zu übermitteln.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Konstruktor für die ResourceNotFoundException.
     * Erzeugt eine neue Ausnahme mit der angegebenen Fehlermeldung, die den Grund der Nichtauffindbarkeit der Ressource beschreibt.
     *
     * @param message Detaillierte Fehlermeldung, die den Grund der Ausnahme erläutert.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
