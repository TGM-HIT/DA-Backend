package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = Consts.BEISPIEL_TABLE_PREFIX + "ITEM")
@Getter
@Setter
@NoArgsConstructor
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
}
