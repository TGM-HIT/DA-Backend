package at.ac.tgm.controller;

import at.ac.tgm.ad.Roles;
import at.ac.tgm.dto.StickGroupDTO;
import at.ac.tgm.dto.USBStickDTO;
import at.ac.tgm.service.StickGroupService;
import at.ac.tgm.service.USBStickService;
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

    public USBStickController(USBStickService usbStickService, StickGroupService stickGroupService) {
        this.usbStickService = usbStickService;
        this.stickGroupService = stickGroupService;
    }

    // --- USBStick Endpoints ---

    @Secured(Roles.TEACHER)
    @GetMapping
    public ResponseEntity<List<USBStickDTO>> getAllUSBSticks() {
        return ResponseEntity.ok(usbStickService.findAll());
    }

    @Secured(Roles.TEACHER)
    @GetMapping("/{inventarnummer}")
    public ResponseEntity<USBStickDTO> getUSBStickById(@PathVariable String inventarnummer) {
        Optional<USBStickDTO> stickOpt = usbStickService.findById(inventarnummer);
        return stickOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured(Roles.TEACHER)
    @PostMapping
    public ResponseEntity<USBStickDTO> addUSBStick(@RequestBody USBStickDTO newStick) {
        USBStickDTO saved = usbStickService.save(newStick);
        return ResponseEntity.ok(saved);
    }

    @Secured(Roles.TEACHER)
    @PutMapping("/{inventarnummer}")
    public ResponseEntity<USBStickDTO> updateUSBStick(@PathVariable String inventarnummer,
                                                      @RequestBody USBStickDTO updatedStick) {
        Optional<USBStickDTO> updatedOpt = usbStickService.update(inventarnummer, updatedStick);
        return updatedOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured(Roles.TEACHER)
    @DeleteMapping("/{inventarnummer}")
    public ResponseEntity<Void> deleteUSBStick(@PathVariable String inventarnummer) {
        usbStickService.deleteById(inventarnummer);
        return ResponseEntity.noContent().build();
    }

    // --- StickGroup Endpoints ---

    @Secured(Roles.TEACHER)
    @GetMapping("/groups")
    public ResponseEntity<List<StickGroupDTO>> getAllGroups() {
        return ResponseEntity.ok(stickGroupService.findAll());
    }

    @Secured(Roles.TEACHER)
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<StickGroupDTO> getGroupById(@PathVariable String groupId) {
        Optional<StickGroupDTO> groupOpt = stickGroupService.findById(groupId);
        return groupOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured(Roles.TEACHER)
    @PostMapping("/groups")
    public ResponseEntity<StickGroupDTO> createGroup(@RequestBody StickGroupDTO newGroup) {
        StickGroupDTO saved = stickGroupService.save(newGroup);
        return ResponseEntity.ok(saved);
    }

    @Secured(Roles.TEACHER)
    @PutMapping("/groups/{groupId}")
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
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId) {
        stickGroupService.deleteById(groupId);
        return ResponseEntity.noContent().build();
    }
}
