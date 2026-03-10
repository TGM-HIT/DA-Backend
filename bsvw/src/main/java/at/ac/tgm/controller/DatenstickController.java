package at.ac.tgm.controller;

import at.ac.tgm.Consts;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.entity.DatenstickEntity;
import at.ac.tgm.service.DatenstickService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Consts.BSVW_PATH_PREFIX + "/datensticks")
@Slf4j
public class DatenstickController {

    @Autowired
    private DatenstickService service;

    @Secured(Roles.TEACHER)
    @GetMapping
    public ResponseEntity<List<DatenstickEntity>> getAll() {
        log.info("Alle Datensticks abrufen");
        return ResponseEntity.ok(service.findAll());
    }

    @Secured(Roles.TEACHER)
    @GetMapping("/{id}")
    public ResponseEntity<DatenstickEntity> getById(@PathVariable Long id) {
        log.info("Datenstick {} abrufen", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @Secured(Roles.TEACHER)
    @PatchMapping("/{id}/status")
    public ResponseEntity<DatenstickEntity> updateStatus(@PathVariable Long id,
                                                         @RequestBody DatenstickEntity entity) {
        log.info("Datenstick {} Status aktualisieren", id);
        DatenstickEntity stick = service.findById(id);
        stick.setStatus(entity.getStatus());
        return ResponseEntity.ok(service.save(stick));
    }
}