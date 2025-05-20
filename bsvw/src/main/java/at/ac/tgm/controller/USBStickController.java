package at.ac.tgm.controller;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.dto.ReservationDTO;
import at.ac.tgm.dto.StickGroupDTO;
import at.ac.tgm.dto.USBStickDTO;
import at.ac.tgm.service.ReservationService;
import at.ac.tgm.service.StickGroupService;
import at.ac.tgm.service.USBStickService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bsvw/usb")
public class USBStickController {

    private final USBStickService usbStickService;
    private final StickGroupService stickGroupService;
    private final ReservationService reservationService;

    public USBStickController(USBStickService usbStickService,
                              StickGroupService stickGroupService,
                              ReservationService reservationService) {
        this.usbStickService = usbStickService;
        this.stickGroupService = stickGroupService;
        this.reservationService = reservationService;
    }

    // --- USBStick Endpoints ---

    @Secured(Roles.TEACHER)
    @GetMapping
    @Operation(summary = "Get all USB sticks")
    public ResponseEntity<List<USBStickDTO>> getAllUSBSticks() {
        return ResponseEntity.ok(usbStickService.findAll());
    }

    @Secured(Roles.TEACHER)
    @GetMapping("/{inventarnummer}")
    @Operation(summary = "Get USB stick by ID")
    public ResponseEntity<USBStickDTO> getUSBStickById(@PathVariable String inventarnummer) {
        Optional<USBStickDTO> stickOpt = usbStickService.findById(inventarnummer);
        return stickOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured(Roles.TEACHER)
    @PostMapping
    @Operation(summary = "Create a new USB stick")
    public ResponseEntity<USBStickDTO> addUSBStick(@RequestBody USBStickDTO newStick) {
        USBStickDTO saved = usbStickService.save(newStick);
        return ResponseEntity.ok(saved);
    }

    @Secured(Roles.TEACHER)
    @PutMapping("/{inventarnummer}")
    @Operation(summary = "Update an existing USB stick")
    public ResponseEntity<USBStickDTO> updateUSBStick(@PathVariable String inventarnummer,
                                                      @RequestBody USBStickDTO updatedStick) {
        Optional<USBStickDTO> updatedOpt = usbStickService.update(inventarnummer, updatedStick);
        return updatedOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured(Roles.TEACHER)
    @DeleteMapping("/{inventarnummer}")
    @Operation(summary = "Delete a USB stick")
    public ResponseEntity<Void> deleteUSBStick(@PathVariable String inventarnummer) {
        usbStickService.deleteById(inventarnummer);
        return ResponseEntity.noContent().build();
    }

    // --- StickGroup Endpoints ---

    @Secured(Roles.TEACHER)
    @GetMapping("/groups")
    @Operation(summary = "Get all stick groups")
    public ResponseEntity<List<StickGroupDTO>> getAllGroups() {
        return ResponseEntity.ok(stickGroupService.findAll());
    }

    @Secured(Roles.TEACHER)
    @GetMapping("/groups/{groupId}")
    @Operation(summary = "Get stick group by ID")
    public ResponseEntity<StickGroupDTO> getGroupById(@PathVariable String groupId) {
        Optional<StickGroupDTO> groupOpt = stickGroupService.findById(groupId);
        return groupOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured(Roles.TEACHER)
    @PostMapping("/groups")
    @Operation(summary = "Create a new stick group")
    public ResponseEntity<StickGroupDTO> createGroup(@RequestBody StickGroupDTO newGroup) {
        StickGroupDTO saved = stickGroupService.save(newGroup);
        return ResponseEntity.ok(saved);
    }

    @Secured(Roles.TEACHER)
    @PutMapping("/groups/{groupId}")
    @Operation(summary = "Update an existing stick group")
    public ResponseEntity<StickGroupDTO> updateGroup(@PathVariable String groupId,
                                                     @RequestBody StickGroupDTO updatedGroup) {
        Optional<StickGroupDTO> existing = stickGroupService.findById(groupId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        updatedGroup.setGroupId(groupId);
        StickGroupDTO saved = stickGroupService.save(updatedGroup);
        return ResponseEntity.ok(saved);
    }

    @Secured(Roles.TEACHER)
    @DeleteMapping("/groups/{groupId}")
    @Operation(summary = "Delete a stick group")
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId) {
        stickGroupService.deleteById(groupId);
        return ResponseEntity.noContent().build();
    }

    // --- Reservation Endpoints ---

    @Secured(Roles.TEACHER)
    @GetMapping("/reservations")
    @Operation(summary = "Get all reservations")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @Secured(Roles.TEACHER)
    @PostMapping("/reservations")
    @Operation(summary = "Create a reservation for a stick group")
    public ResponseEntity<ReservationDTO> createReservation(@RequestBody ReservationDTO reservationDTO) {
        ReservationDTO saved = reservationService.save(reservationDTO);
        return ResponseEntity.ok(saved);
    }

    @Secured(Roles.TEACHER)
    @DeleteMapping("/reservations/{id}")
    @Operation(summary = "Delete a reservation")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
