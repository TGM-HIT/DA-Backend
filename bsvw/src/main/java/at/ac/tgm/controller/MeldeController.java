package at.ac.tgm.controller;

import at.ac.tgm.Consts;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.entity.MeldeEntity;
import at.ac.tgm.entity.BootstickEntity;
import at.ac.tgm.enums.Schulklasse;
import at.ac.tgm.enums.Status;
import at.ac.tgm.enums.Zustand;
import at.ac.tgm.repository.BootstickRepository;
import at.ac.tgm.service.MeldeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(Consts.BSVW_PATH_PREFIX + "/melden")
@Slf4j
public class MeldeController {

    @Autowired
    private MeldeService service;
    @Autowired
    private BootstickRepository bootstickRepository;

    @Secured(Roles.TEACHER)
    @GetMapping
    public ResponseEntity<List<MeldeEntity>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Secured(Roles.TEACHER)
    @PostMapping("/bootstick/{name}/{nummer}")
    public ResponseEntity<MeldeEntity> melden(@PathVariable Schulklasse name,
                              @PathVariable int nummer,
                              @RequestParam(defaultValue = "FEHLERHAFT") Status typ,
                              @RequestBody MeldeEntity meldung) {
        BootstickEntity stick = bootstickRepository
                .findByNameAndNummer(name, nummer)
                .orElseThrow(() -> new NoSuchElementException("Bootstick nicht gefunden"));
        
        if (typ == Status.VERLOREN) {
            stick.setStatus(Status.VERLOREN);
        } else {
            stick.setZustand(Zustand.FEHLERHAFT);
        }
        bootstickRepository.save(stick);
        
        meldung.setBootstick(stick);
        meldung.setDatum(LocalDateTime.now());
        MeldeEntity saved = service.save(meldung);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}