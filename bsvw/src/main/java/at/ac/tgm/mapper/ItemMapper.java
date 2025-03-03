package at.ac.tgm.mapper;

import at.ac.tgm.dto.ItemDto;
import at.ac.tgm.entity.ItemEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto entityToDto(ItemEntity entity);
    List<ItemDto> entityToDto(List<ItemEntity> entities);
    ItemEntity dtoToEntity(ItemDto dto);
    List<ItemEntity> dtoToEntity(List<ItemDto> dtos);
    
}
