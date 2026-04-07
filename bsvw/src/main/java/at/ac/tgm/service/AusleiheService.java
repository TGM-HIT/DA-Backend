package at.ac.tgm.service;

import at.ac.tgm.entity.AusleiheEntity;
import at.ac.tgm.entity.BootstickEntity;
import at.ac.tgm.entity.DatenstickEntity;
import at.ac.tgm.enums.Status;
import at.ac.tgm.repository.AusleiheRepository;
import at.ac.tgm.repository.BootstickRepository;
import at.ac.tgm.repository.DatenstickRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class AusleiheService {

    @Autowired
    private AusleiheRepository repository;
    @Autowired
    private BootstickRepository bootstickRepository;
    @Autowired
    private DatenstickRepository datenstickRepository;

    public List<AusleiheEntity> findAll() {
        return repository.findAll();
    }

    public AusleiheEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ausleihe nicht gefunden"));
    }

    public AusleiheEntity save(AusleiheEntity entity) {
        log.info("Speichere Ausleihe: {}", entity);
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        log.info("Lösche Ausleihe mit ID: {}", id);
        repository.deleteById(id);
    }
    public AusleiheEntity beanspruchen(Long id) {

        AusleiheEntity ausleihe = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ausleihe nicht gefunden"));

        for (BootstickEntity stick : ausleihe.getBootsticks()) {

            BootstickEntity dbStick = bootstickRepository.findById(stick.getId())
                    .orElseThrow(() -> new NoSuchElementException("Bootstick nicht gefunden"));

            if (dbStick.getStatus() != Status.VORHANDEN) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Bootstick bereits vergeben: " + dbStick.getName() + dbStick.getNummer()
                );
            }
            dbStick.setStatus(Status.AUSGEBORGT);
            bootstickRepository.save(dbStick);
        }
        for (DatenstickEntity stick : ausleihe.getDatensticks()) {

            DatenstickEntity dbStick = datenstickRepository.findById(stick.getId())
                    .orElseThrow(() -> new NoSuchElementException("Datenstick nicht gefunden"));

            if (dbStick.getStatus() != Status.VORHANDEN) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Datenstick bereits vergeben: " + dbStick.getName() + dbStick.getNummer()
                );
            }
            dbStick.setStatus(Status.AUSGEBORGT);
            datenstickRepository.save(dbStick);
        }
        ausleihe.setAusleihedatum(LocalDateTime.now());
        return repository.save(ausleihe);
    }
    public AusleiheEntity zurueckgeben(Long id) {

        AusleiheEntity ausleihe = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ausleihe nicht gefunden"));

        for (BootstickEntity stick : ausleihe.getBootsticks()) {

            BootstickEntity dbStick = bootstickRepository.findById(stick.getId())
                    .orElseThrow(() -> new NoSuchElementException("Bootstick nicht gefunden"));

            if (dbStick.getStatus() == Status.AUSGEBORGT) {
                dbStick.setStatus(Status.VORHANDEN);
                bootstickRepository.save(dbStick);
            }

        }
        for (DatenstickEntity stick : ausleihe.getDatensticks()) {

            DatenstickEntity dbStick = datenstickRepository.findById(stick.getId())
                    .orElseThrow(() -> new NoSuchElementException("Datenstick nicht gefunden"));

            if (dbStick.getStatus() == Status.AUSGEBORGT) {
                dbStick.setStatus(Status.VORHANDEN);
                datenstickRepository.save(dbStick);
            }

        }
        ausleihe.setRueckgabedatum(LocalDateTime.now());
        return repository.save(ausleihe);
    }
}