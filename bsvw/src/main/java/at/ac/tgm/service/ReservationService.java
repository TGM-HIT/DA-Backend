package at.ac.tgm.service;

import at.ac.tgm.dto.ReservationDTO;
import at.ac.tgm.dto.StickGroupDTO;
import at.ac.tgm.entity.Reservation;
import at.ac.tgm.entity.StickGroup;
import at.ac.tgm.mapper.ReservationMapper;
import at.ac.tgm.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository repository;

    @Autowired
    private ReservationMapper mapper;

    @Autowired
    private StickGroupService stickGroupService;

    public List<ReservationDTO> findAll() {
        return repository.findAll().stream().map(mapper::toDTO).toList();
    }

    public ReservationDTO save(ReservationDTO dto) {
        // Gruppe aus DB holen
        StickGroupDTO group = stickGroupService.findById(dto.getGroupId())
                .orElseThrow(() -> new RuntimeException("Gruppe nicht gefunden"));

        Reservation reservation = mapper.toEntity(dto);
        reservation.setGroup(group);
        return mapper.toDTO(repository.save(reservation));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}

