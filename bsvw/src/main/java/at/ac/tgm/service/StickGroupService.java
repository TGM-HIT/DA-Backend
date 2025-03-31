package at.ac.tgm.service;

import at.ac.tgm.dto.StickGroupDTO;
import at.ac.tgm.entity.StickGroup;
import at.ac.tgm.mapper.StickGroupMapper;
import at.ac.tgm.repository.StickGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StickGroupService {

    @Autowired
    private StickGroupRepository repository;

    @Autowired
    private StickGroupMapper mapper;

    public List<StickGroupDTO> findAll() {
        return mapper.toDTO(repository.findAll());
    }

    public Optional<StickGroupDTO> findById(String groupId) {
        return repository.findById(groupId).map(mapper::toDTO);
    }

    public StickGroupDTO save(StickGroupDTO dto) {
        StickGroup entity = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(entity));
    }

    public void deleteById(String groupId) {
        repository.deleteById(groupId);
    }
}
