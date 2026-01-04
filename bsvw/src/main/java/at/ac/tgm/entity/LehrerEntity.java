package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "lehrer")
@Getter
@Setter
@NoArgsConstructor
public class LehrerEntity {
    //ID für schnellere verarbeitung bei ManyToMany beziehung mit der Ausleihe
    @Id
    @GeneratedValue
    private Long id;
    //Der Accountname wie im LDAP
    @Column(unique = true)
    private String samAccountName;
    //Displayname vom LDAP
    private String displayName;
    //Email vom LDAP
    private String email;
}

