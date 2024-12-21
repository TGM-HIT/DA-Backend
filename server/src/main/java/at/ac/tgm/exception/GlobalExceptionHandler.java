package at.ac.tgm.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.expression.spel.ast.MethodReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.CommunicationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.ExpressionAuthorizationDecision;
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
public class GlobalExceptionHandler {
    private final HttpHeaders headers = new HttpHeaders();
    
    GlobalExceptionHandler() {
        // Wäre sonst wegen @ResponseBody standardmäßig APPLICATION_JSON
        headers.setContentType(MediaType.TEXT_PLAIN);
    }
    
    private static List<String> getRequiredRoles(AuthorizationDeniedException e) {
        if (e.getAuthorizationResult() instanceof ExpressionAuthorizationDecision result) {
            if (result.getExpression() instanceof SpelExpression expression) {
                if (expression.getAST() instanceof MethodReference ast) {
                    List<String> authorities = new ArrayList<>(ast.getChildCount());
                    for (int i = 0; i < ast.getChildCount(); i++) {
                        authorities.add(ast.getChild(i).toStringAST());
                    }
                    return authorities;
                }
            }
        }
        return null;
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handle(MethodArgumentNotValidException e) {
        List<String> list = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            list.add(error.getField() + " " + error.getDefaultMessage());
        }
        String body = String.join("\n", list);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handler(IllegalArgumentException e) {
        String body = e.getMessage();
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handler(NoSuchElementException e) {
        String body = e.getMessage();
        return new ResponseEntity<>(body, headers, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handler(ResponseStatusException e) {
        String body = e.getReason() != null ? e.getReason() : e.getMessage();
        return new ResponseEntity<>(body, headers, e.getStatusCode());
    }
    
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<String> handler(AuthorizationDeniedException e, HttpServletRequest request) {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return new ResponseEntity<>("Unauthorized", headers, HttpStatus.UNAUTHORIZED);
        } else {
            List<String> roles = getRequiredRoles(e);
            String body = "Not allowed for your role";
            if (roles != null && !roles.isEmpty()) {
                body += ", one of the following roles required: " + String.join(", ", roles);
            }
            return new ResponseEntity<>(body, headers, HttpStatus.FORBIDDEN);
        }
    }
    
    @ExceptionHandler(CommunicationException.class)
    public ResponseEntity<String> handler(CommunicationException e) {
        String body = "Es konnte keine Verbindung zu AD LDAP hergestellt werden, wahrscheinlich sind Sie nicht mit dem VPN verbunden!";
        System.err.println(body);
        return new ResponseEntity<>(body, headers, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handler(Exception e) {
        e.printStackTrace();
        String body = e.getMessage();
        return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
