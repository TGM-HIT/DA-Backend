package at.ac.tgm.controller;

import at.ac.tgm.Consts;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.entity.AusleiheEntity;
import at.ac.tgm.service.AusleiheService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Consts.BSVW_PATH_PREFIX + "/ausleihen")
@Slf4j
public class AusleiheController {

    @Autowired
    private AusleiheService service;

    @Secured(Roles.TEACHER)
    @GetMapping
    public ResponseEntity<List<AusleiheEntity>> getAll() {
        log.info("Alle Ausleihen abrufen");
        return ResponseEntity.ok(service.findAll());
    }

    @Secured(Roles.TEACHER)
    @GetMapping("/{id}")
    public ResponseEntity<AusleiheEntity> getById(@PathVariable Long id) {
        log.info("Ausleihe {} abrufen", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @Secured(Roles.TEACHER)
    @PostMapping
    public ResponseEntity<AusleiheEntity> create(@RequestBody @Valid AusleiheEntity entity) {
        log.info("Neue Ausleihe erstellen");
        AusleiheEntity saved = service.save(entity);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Secured(Roles.TEACHER)
    @PutMapping("/{id}")
    public ResponseEntity<AusleiheEntity> update(
            @PathVariable Long id,
            @RequestBody @Valid AusleiheEntity entity) {

        log.info("Ausleihe {} aktualisieren", id);

        entity.setId(id);
        return ResponseEntity.ok(service.save(entity));
    }

    @Secured(Roles.TEACHER)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        log.info("Ausleihe {} löschen", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Secured(Roles.TEACHER)
    @PostMapping("/{id}/beanspruchen")
    public ResponseEntity<AusleiheEntity> beanspruchen(@PathVariable Long id) {

        log.info("Ausleihe {} wird beansprucht", id);

        return ResponseEntity.ok(service.beanspruchen(id));
    }

    @Secured(Roles.TEACHER)
    @PostMapping("/{id}/zurueckgeben")
    public ResponseEntity<AusleiheEntity> zurueckgeben(@PathVariable Long id) {

        log.info("Ausleihe {} wird zurückgegeben", id);

        return ResponseEntity.ok(service.zurueckgeben(id));
    }
}