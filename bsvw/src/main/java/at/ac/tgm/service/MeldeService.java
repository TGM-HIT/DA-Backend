package at.ac.tgm.service;

import at.ac.tgm.entity.MeldeEntity;
import at.ac.tgm.repository.MeldeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeldeService {
    private final MeldeRepository repository;
    public List<MeldeEntity> findAll() {
        return repository.findAll();
    }
    public List<MeldeEntity> findByBootstick(Long bootstickId) {
        return repository.findByBootstickId(bootstickId);
    }
    public MeldeEntity save(MeldeEntity entity) {
        log.info("Speichere Defektmeldung {}", entity);
        return repository.save(entity);
    }
}