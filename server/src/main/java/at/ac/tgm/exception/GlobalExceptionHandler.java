package at.ac.tgm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.CommunicationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@ControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    private final HttpHeaders headers = new HttpHeaders();

    GlobalExceptionHandler() {
        headers.setContentType(MediaType.TEXT_PLAIN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handle(MethodArgumentNotValidException e) {
        List<String> list = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            list.add(error.getField() + " " + error.getDefaultMessage());
        }
        String body = String.join("\n", list);
        log.info("MethodArgumentNotValidException {}", e.getMessage());
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handler(IllegalArgumentException e) {
        log.info("IllegalArgumentException {}", e.getMessage());
        String body = e.getMessage();
        System.out.println("IllegalArgumentException: " + e.getMessage());
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handler(NoSuchElementException e) {
        log.info("NoSuchElementException {}", e.getMessage());
        String body = e.getMessage();
        return new ResponseEntity<>(body, headers, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handler(ResponseStatusException e) {
        log.info("ResponseStatusException {}", e.getMessage());
        String body = e.getReason() != null ? e.getReason() : e.getMessage();
        return new ResponseEntity<>(body, headers, e.getStatusCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handler(AccessDeniedException e) {
        log.info("AccessDeniedException {}", e.getMessage());
        String body = e.getMessage();
        return new ResponseEntity<>(body, headers, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CommunicationException.class)
    public ResponseEntity<String> handler(CommunicationException e) {
        log.error("CommunicationException {}", e.getMessage());
        String body = "Es konnte keine Verbindung zu AD LDAP hergestellt werden, wahrscheinlich sind Sie nicht mit dem VPN verbunden!";
        return new ResponseEntity<>(body, headers, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handle(BadCredentialsException e) {
        log.error("BadCredentialsException {}", e.getMessage());
        String body = e.getMessage();
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handle(UsernameNotFoundException e) {
        log.error("UsernameNotFoundException {}", e.getMessage());
        String body = e.getMessage();
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handle(AuthenticationException e) {
        log.error("AuthenticationException {}", e.getMessage());
        String body = "Username oder Password ungültig";
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handler(Exception e) {
        log.error("Exception {}", e.getMessage());
        String body = e.getMessage();
        return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}