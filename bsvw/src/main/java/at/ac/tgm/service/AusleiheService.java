package at.ac.tgm.service;

import at.ac.tgm.entity.AusleiheEntity;
import at.ac.tgm.repository.AusleiheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AusleiheService {

    private final AusleiheRepository repository;

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
}