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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
                .orElseThrow(() -> new RuntimeException("Ausleihe nicht gefunden"));
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
                .orElseThrow(() -> new RuntimeException("Ausleihe nicht gefunden"));

        for (BootstickEntity stick : ausleihe.getBootsticks()) {

            BootstickEntity dbStick = bootstickRepository.findById(stick.getId())
                    .orElseThrow(() -> new RuntimeException("Bootstick nicht gefunden"));

            if (dbStick.getStatus() != Status.VORHANDEN) {
                throw new RuntimeException(
                        "Bootstick bereits vergeben: "
                                + dbStick.getName() + dbStick.getNummer()
                );
            }
            dbStick.setStatus(Status.AUSGEBORGT);
        }
        for (DatenstickEntity stick : ausleihe.getDatensticks()) {

            DatenstickEntity dbStick = datenstickRepository.findById(stick.getId())
                    .orElseThrow(() -> new RuntimeException("Datenstick nicht gefunden"));

            if (dbStick.getStatus() != Status.VORHANDEN) {
                throw new RuntimeException(
                        "Datenstick bereits vergeben: "
                                + dbStick.getName() + dbStick.getNummer()
                );
            }
            dbStick.setStatus(Status.AUSGEBORGT);
        }
        ausleihe.setAusleihedatum(LocalDateTime.now());
        return repository.save(ausleihe);
    }
    public AusleiheEntity zurueckgeben(Long id) {

        AusleiheEntity ausleihe = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ausleihe nicht gefunden"));

        for (BootstickEntity stick : ausleihe.getBootsticks()) {

            BootstickEntity dbStick = bootstickRepository.findById(stick.getId())
                    .orElseThrow(() -> new RuntimeException("Bootstick nicht gefunden"));

            if (dbStick.getStatus() == Status.AUSGEBORGT) {
                dbStick.setStatus(Status.VORHANDEN);
            }

        }
        for (DatenstickEntity stick : ausleihe.getDatensticks()) {

            DatenstickEntity dbStick = datenstickRepository.findById(stick.getId())
                    .orElseThrow(() -> new RuntimeException("Datenstick nicht gefunden"));

            if (dbStick.getStatus() == Status.AUSGEBORGT) {
                dbStick.setStatus(Status.VORHANDEN);
            }

        }
        ausleihe.setRueckgabedatum(LocalDateTime.now());
        return repository.save(ausleihe);
    }
}