package at.ac.tgm.service;

import at.ac.tgm.dto.ItemDto;
import at.ac.tgm.mapper.ItemMapper;
import at.ac.tgm.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    
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
