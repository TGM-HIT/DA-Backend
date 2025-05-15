package at.ac.tgm.mapper;

import at.ac.tgm.dto.USBStickDTO;
import at.ac.tgm.entity.USBStick;
import at.ac.tgm.entity.StickGroup;
import at.ac.tgm.repository.StickGroupRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class USBStickMapper {

    @Autowired
    protected StickGroupRepository groupRepository;

    public abstract USBStick toEntity(USBStickDTO dto);

    public abstract USBStickDTO toDTO(USBStick entity);

    public abstract List<USBStickDTO> toDTO(List<USBStick> entity);

    public abstract List<USBStick> toEntity(List<USBStickDTO> dto);

    @AfterMapping
    protected void mapGroupIdToGroup(USBStickDTO dto, @MappingTarget USBStick entity) {
        if (dto.getGroupId() != null) {
            StickGroup group = groupRepository.findById(dto.getGroupId()).orElse(null);
            entity.setGroup(group);
        }
    }

    @AfterMapping
    protected void mapGroupToGroupId(USBStick entity, @MappingTarget USBStickDTO dto) {
        if (entity.getGroup() != null) {
            dto.setGroupId(entity.getGroup().getGroupId());
        }
    }
}
