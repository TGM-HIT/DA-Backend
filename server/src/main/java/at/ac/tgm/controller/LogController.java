package at.ac.tgm.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/logs")
public class LogController {
    @GetMapping(value = "", produces = MediaType.TEXT_HTML_VALUE)
    public String getLogs() {
        return "<html><body><ul>" + Stream.of(new File("logs/").listFiles())
                .filter(f -> !f.isDirectory())
                .map(f -> "<li><a href=\"/logs/" + f.getName() + "\">" + f.getName() + "</a></li>")
                .collect(Collectors.joining()) + "</ul></body></html>";
    }
    
    @GetMapping(value = "/{name}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getLog(@PathVariable String name) {
        if (name.contains("/")) {
            throw new IllegalArgumentException("Not allowed");
        }
        File file = new File("logs/" + name);
        if (file.exists()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body><pre>");
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
