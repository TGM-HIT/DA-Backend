package at.ac.tgm.controller;

import at.ac.tgm.Consts;
import at.ac.tgm.ad.Roles;
import at.ac.tgm.dto.ItemDto;
import at.ac.tgm.service.ItemService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Consts.BEISPIEL_PATH_PREFIX + "/item")
@Slf4j
public class ItemController {
    @Autowired
    private ItemService service;
    
    @Secured(Roles.TEACHER)
    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems() {
        return ResponseEntity.ok(service.getAllItems());
    }
    
    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestBody @Valid ItemDto itemDto) {
        return new ResponseEntity<>(service.create(itemDto), HttpStatus.CREATED);
    }
}
