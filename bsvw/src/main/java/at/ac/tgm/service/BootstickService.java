package at.ac.tgm.service;

import at.ac.tgm.entity.BootstickEntity;
import at.ac.tgm.repository.BootstickRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
@Slf4j
public class BootstickService {

    @Autowired
    private BootstickRepository repository;

    public List<BootstickEntity> findAll() {
        return repository.findAll();
    }

    public BootstickEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bootstick nicht gefunden"));
    }

    public BootstickEntity save(BootstickEntity entity) {
        log.info("Speichere Bootstick: {}", entity);
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        log.info("Lösche Bootstick mit ID: {}", id);
        repository.deleteById(id);
    }
}