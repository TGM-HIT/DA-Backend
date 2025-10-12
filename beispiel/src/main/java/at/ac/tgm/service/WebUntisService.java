package at.ac.tgm.service;

import at.ac.tgm.webuntis.WebUntisSession;
import org.bytedream.untis4j.responseObjects.Rooms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WebUntisService {
    private WebUntisSession webUntisSession;
    
    @Autowired
    public WebUntisService(WebUntisSession webUntisSession) {
        this.webUntisSession = webUntisSession;
    }
    
    public Rooms get() throws IOException {
        return webUntisSession.getSession().getRooms();
    }
}
