package at.ac.tgm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private Long reservationId;
    private String groupId;
    private String reservedBy;
    private String reservationDate;
}