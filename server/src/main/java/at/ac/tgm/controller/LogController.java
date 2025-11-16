package at.ac.tgm.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/logs")
@Hidden
public class LogController {
    @Value("${secret}")
    private String secret;
    
    @GetMapping(value = "", produces = MediaType.TEXT_HTML_VALUE)
    public String getLogs(@RequestParam String secret) {
        if (!secret.equals(this.secret)) {
            throw new IllegalArgumentException("Wrong secret");
        }
        return "<html><body><ul>" + Stream.of(new File("logs/").listFiles())
                .filter(f -> !f.isDirectory())
                .map(f -> "<li><a href=\"/logs/" + f.getName() + "?secret=" + secret + "\">" + f.getName() + "</a></li>")
                .collect(Collectors.joining()) + "</ul></body></html>";
    }
    
    @GetMapping(value = "/{name}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getLog(@PathVariable String name, @RequestParam String secret) {
        if (!secret.equals(this.secret)) {
            throw new IllegalArgumentException("Wrong secret");
        }
        if (name.contains("/")) {
            throw new IllegalArgumentException("Not allowed");
        }
        File file = new File("logs/" + name);
        if (file.exists()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body><style>white-space: pre-wrap; white-space: -moz-pre-wrap; white-space: -pre-wrap;  white-space: -o-pre-wrap; word-wrap: break-word;</style><pre>");
            try (Scanner s = new Scanner(file)) {
                while (s.hasNextLine()) {
                    sb.append(s.nextLine()).append(System.lineSeparator());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sb.append("</pre></body></html>");
            return ResponseEntity.ok().body(sb.toString());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
