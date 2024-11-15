package at.ac.tgm.controller;

import at.ac.tgm.Consts;
import at.ac.tgm.dto.ItemDto;
import at.ac.tgm.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Consts.BEISPIEL_PATH_PREFIX + "/item")
public class ItemController {
    @Autowired
    private ItemService service;
    
    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems() {
        return ResponseEntity.ok(service.getAllItems());
    }
    
    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestBody @Valid ItemDto itemDto) {
        return new ResponseEntity<>(service.create(itemDto), HttpStatus.CREATED);
    }
}
