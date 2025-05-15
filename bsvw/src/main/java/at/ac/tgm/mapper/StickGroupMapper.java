package at.ac.tgm.mapper;

import at.ac.tgm.dto.StickGroupDTO;
import at.ac.tgm.entity.StickGroup;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StickGroupMapper {
    StickGroup toEntity(StickGroupDTO dto);
    StickGroupDTO toDTO(StickGroup entity);
    List<StickGroupDTO> toDTO(List<StickGroup> entities);
    List<StickGroup> toEntity(List<StickGroupDTO> dtos);
}
