package at.ac.tgm.service;

import at.ac.tgm.dto.ItemDto;
import at.ac.tgm.mapper.ItemMapper;
import at.ac.tgm.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ItemService {
    @Autowired
    private ItemRepository repository;
    @Autowired
    private ItemMapper mapper;
    
    public List<ItemDto> getAllItems() {
        return mapper.entityToDto(repository.findAll());
    }
    
    public ItemDto create(ItemDto itemDto) {
        return mapper.entityToDto(repository.save(mapper.dtoToEntity(itemDto)));
    }
}
