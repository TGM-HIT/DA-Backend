package at.ac.tgm.mapper;

import at.ac.tgm.dto.LehrerDto;
import at.ac.tgm.entity.LehrerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LehrerMapper {

    LehrerDto toDto(LehrerEntity entity);

    LehrerEntity toEntity(LehrerDto dto);
}
