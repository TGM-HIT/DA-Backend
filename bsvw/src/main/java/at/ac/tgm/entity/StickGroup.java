package at.ac.tgm.entity;

import at.ac.tgm.Consts;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = Consts.BSVW_TABLE_PREFIX + "stick_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StickGroup {

    @Id
    private String groupId;

    private String stickType;

    // Wird nicht direkt vom Request gesetzt – automatisch berechnet
    private int numberOfSticks;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<USBStick> sticks = new ArrayList<>();

    public void recalcStickCount() {
        this.numberOfSticks = (this.sticks == null) ? 0 : this.sticks.size();
    }

    public void addStick(USBStick stick) {
        if (this.sticks == null) {
            this.sticks = new ArrayList<>();
        }
        this.sticks.add(stick);
        stick.setGroup(this);
        recalcStickCount();
    }
}
