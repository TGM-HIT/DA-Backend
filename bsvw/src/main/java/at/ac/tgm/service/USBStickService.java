package at.ac.tgm.service;

import at.ac.tgm.dto.USBStickDTO;
import at.ac.tgm.entity.USBStick;
import at.ac.tgm.mapper.USBStickMapper;
import at.ac.tgm.repository.USBStickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class USBStickService {

    @Autowired
    private USBStickRepository repository;

    @Autowired
    private USBStickMapper mapper;

    public List<USBStickDTO> findAll() {
        return mapper.toDTO(repository.findAll());
    }

    public Optional<USBStickDTO> findById(String inventarnummer) {
        return repository.findById(inventarnummer)
                .map(mapper::toDTO);
    }

    public USBStickDTO save(USBStickDTO dto) {
        return mapper.toDTO(repository.save(mapper.toEntity(dto)));
    }

    public void deleteById(String inventarnummer) {
        repository.deleteById(inventarnummer);
    }

    public Optional<USBStickDTO> update(String inventarnummer, USBStickDTO updatedDTO) {
        Optional<USBStick> existingOpt = repository.findById(inventarnummer);
        if (existingOpt.isPresent()) {
            USBStick existing = existingOpt.get();
            // Felder aktualisieren
            existing.setTyp(updatedDTO.getTyp());
            existing.setSpeicherkapazitaet(updatedDTO.getSpeicherkapazitaet());
            existing.setHersteller(updatedDTO.getHersteller());
            existing.setModell(updatedDTO.getModell());
            existing.setSeriennummer(updatedDTO.getSeriennummer());
            existing.setVerfuegbarkeit(updatedDTO.getVerfuegbarkeit());
            existing.setZustand(updatedDTO.getZustand());
            // Gruppen-Zuordnung über Mapper übernehmen
            USBStick tempEntity = mapper.toEntity(updatedDTO);
            existing.setGroup(tempEntity.getGroup());
            USBStick saved = repository.save(existing);
            return Optional.of(mapper.toDTO(saved));
        }
        return Optional.empty();
    }
}
