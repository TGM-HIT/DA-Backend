package at.ac.tgm.service;

import at.ac.tgm.entity.DatenstickEntity;
import at.ac.tgm.repository.DatenstickRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatenstickService {

    private final DatenstickRepository repository;

    public List<DatenstickEntity> findAll() {
        return repository.findAll();
    }

    public DatenstickEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Datenstick nicht gefunden"));
    }

    public DatenstickEntity save(DatenstickEntity entity) {
        log.info("Speichere Datenstick: {}", entity);
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        log.info("Lösche Datenstick mit ID: {}", id);
        repository.deleteById(id);
    }
}