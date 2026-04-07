package at.ac.tgm.controller;

import at.ac.tgm.Consts;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.entity.AusleiheEntity;
import at.ac.tgm.entity.RueckgabeEntity;
import at.ac.tgm.service.RueckgabeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Consts.BSVW_PATH_PREFIX + "/rueckgaben")
@Slf4j
public class RueckgabeController {

    @Autowired
    private RueckgabeService service;

    @Secured(Roles.TEACHER)
    @GetMapping
    public ResponseEntity<List<RueckgabeEntity>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Secured(Roles.TEACHER)
    @GetMapping("/{id}")
    public ResponseEntity<RueckgabeEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Secured(Roles.TEACHER)
    @PostMapping
    public ResponseEntity<RueckgabeEntity> create(@RequestBody @Valid RueckgabeEntity entity) {
        RueckgabeEntity saved = service.save(entity);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

}