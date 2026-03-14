package at.ac.tgm.mapper;

import at.ac.tgm.dto.LehrerDto;
import at.ac.tgm.entity.LehrerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LehrerMapper {

    LehrerDto toDto(LehrerEntity entity);

    @Mapping(target = "id", ignore = true)
    LehrerEntity toEntity(LehrerDto dto);
}
