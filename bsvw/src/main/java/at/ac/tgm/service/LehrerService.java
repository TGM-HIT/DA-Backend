package at.ac.tgm.service;

import at.ac.tgm.dto.LehrerDto;
import at.ac.tgm.entity.LehrerEntity;
import at.ac.tgm.mapper.LehrerMapper;
import at.ac.tgm.repository.LehrerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LehrerService {

    private final LehrerRepository repository;
    private final LehrerMapper mapper;

    public LehrerDto getBySamAccountName(String sam) {
        LehrerEntity entity = repository.findBySamAccountName(sam)
                .orElseThrow(() -> new RuntimeException("Lehrer nicht gefunden"));

        return mapper.toDto(entity);
    }
}