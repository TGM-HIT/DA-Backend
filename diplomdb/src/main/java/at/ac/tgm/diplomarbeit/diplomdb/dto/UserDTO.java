package at.ac.tgm.diplomarbeit.diplomdb.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserDTO {
    private String sAMAccountName;
    private String cn;
    private String sn;
    private String displayName;
    private String mail;
    private Set<GroupDTO> groups;
}
