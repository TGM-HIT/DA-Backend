package at.ac.tgm.controller;

import at.ac.tgm.Consts;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.entity.BootstickEntity;
import at.ac.tgm.service.BootstickService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Consts.BSVW_PATH_PREFIX + "/bootsticks")
@Slf4j
public class BootstickController {

    @Autowired
    private BootstickService service;

    @Secured(Roles.TEACHER)
    @GetMapping
    public ResponseEntity<List<BootstickEntity>> getAll() {
        log.info("Alle Bootsticks abrufen");
        return ResponseEntity.ok(service.findAll());
    }

    @Secured(Roles.TEACHER)
    @GetMapping("/{id}")
    public ResponseEntity<BootstickEntity> getById(@PathVariable Long id) {
        log.info("Bootstick {} abrufen", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @Secured(Roles.TEACHER)
    @PatchMapping("/{id}/status")
    public ResponseEntity<BootstickEntity> updateStatus(@PathVariable Long id,
                                                        @RequestBody BootstickEntity entity) {
        log.info("Bootstick {} Status aktualisieren", id);
        BootstickEntity stick = service.findById(id);
        stick.setStatus(entity.getStatus());
        return ResponseEntity.ok(service.save(stick));
    }

    @Secured(Roles.TEACHER)
    @PatchMapping("/{id}/zustand")
    public ResponseEntity<BootstickEntity> updateZustand(@PathVariable Long id,
                                                         @RequestBody BootstickEntity entity) {
        log.info("Bootstick {} Zustand aktualisieren", id);
        BootstickEntity stick = service.findById(id);
        stick.setZustand(entity.getZustand());
        return ResponseEntity.ok(service.save(stick));
    }
}