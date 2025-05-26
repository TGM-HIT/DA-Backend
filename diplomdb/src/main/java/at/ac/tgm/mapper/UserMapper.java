package at.ac.tgm.mapper;

import at.ac.tgm.ad.entry.GroupEntry;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.dto.GroupDTO;
import at.ac.tgm.dto.UserDTO;

import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {}

    /**
     * Wandelt ein UserEntry in ein UserDTO um.
     *
     * @param user Das UserEntry-Objekt.
     * @return Das resultierende UserDTO mit den entsprechenden Benutzerinformationen.
     */
    public static UserDTO toDTO(UserEntry user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setSAMAccountName(user.getSAMAccountName());
        dto.setCn(user.getCn());
        dto.setSn(user.getSn());
        dto.setDisplayName(user.getDisplayName());
        dto.setMail(user.getMail());

        if (user.getGroups() != null) {
            dto.setGroups(user.getGroups().stream()
                    .map(UserMapper::toDTO)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    /**
     * Wandelt ein GroupEntry in ein GroupDTO um.
     *
     * @param g Das GroupEntry-Objekt.
     * @return Das resultierende GroupDTO mit den entsprechenden Gruppendaten.
     */
    public static GroupDTO toDTO(GroupEntry g) {
        GroupDTO dto = new GroupDTO();
        dto.setCn(g.getCn());
        dto.setDisplayName(g.getDisplayName());
        return dto;
    }
}
