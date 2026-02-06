package at.ac.tgm.service;

import at.ac.tgm.entity.RueckgabeEntity;
import at.ac.tgm.repository.RueckgabeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RueckgabeService {

    private final RueckgabeRepository repository;

    public List<RueckgabeEntity> findAll() {
        return repository.findAll();
    }

    public RueckgabeEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rueckgabe nicht gefunden"));
    }

    public RueckgabeEntity save(RueckgabeEntity entity) {
        log.info("Speichere Rueckgabe: {}", entity);
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        log.info("Lösche Rueckgabe mit ID: {}", id);
        repository.deleteById(id);
    }
}