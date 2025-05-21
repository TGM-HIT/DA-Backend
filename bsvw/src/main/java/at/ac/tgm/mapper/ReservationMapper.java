package at.ac.tgm.mapper;

import at.ac.tgm.dto.ReservationDTO;
import at.ac.tgm.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(source = "group.groupId", target = "groupId")
    ReservationDTO toDTO(Reservation entity);

    @Mapping(source = "groupId", target = "group.groupId")
    Reservation toEntity(ReservationDTO dto);
}

