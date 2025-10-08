package at.ac.tgm.controller;

import at.ac.tgm.Consts;
import at.ac.tgm.service.WebUntisService;
import lombok.extern.slf4j.Slf4j;
import org.bytedream.untis4j.responseObjects.Rooms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(Consts.BEISPIEL_PATH_PREFIX + "/webuntis")
@Slf4j
public class WebUntisController {
    private WebUntisService webUntisService;
    
    @Autowired
    public WebUntisController(WebUntisService webUntisService) {
        this.webUntisService = webUntisService;
    }
    
    @GetMapping
    public Rooms get() throws IOException {
        return webUntisService.get();
    }
}
