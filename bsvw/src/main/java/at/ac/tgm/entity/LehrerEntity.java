package at.ac.tgm.entity;

import org.hibernate.annotations.Comment;

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
    @Comment("ID für schnellere verarbeitung bei ManyToMany beziehung mit der Ausleihe")
    @Id
    @GeneratedValue
    private Long id;
    @Comment("Der Accountname wie im LDAP")
    @Column(unique = true)
    private String samAccountName;
    @Comment("Displayname vom LDAP")
    private String displayName;
    @Comment("Email vom LDAP")
    private String email;
}

