package at.ac.tgm.controller;

import lombok.extern.slf4j.Slf4j;
import org.bytedream.untis4j.Session;
import org.bytedream.untis4j.UntisUtils;
import org.bytedream.untis4j.responseObjects.Rooms;
import org.bytedream.untis4j.responseObjects.Teachers;
import org.bytedream.untis4j.responseObjects.WeeklyTimetable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/webuntis")
public class WebUntisDemoController {
    private final Session session;
    
    @Autowired
    public WebUntisDemoController(Session session) {
        this.session = session;
    }
    
    @GetMapping("/weeklyTimetable/person/{id}")
    public String getWeeklyTimetableOfPerson(@PathVariable int id) throws IOException {
        WeeklyTimetable weeklyTimetableFromPersonId = session.getWeeklyTimetable(LocalDate.now(), UntisUtils.ElementType.PERSON, id);
        return weeklyTimetableFromPersonId.toString();
    }
    
    @GetMapping("/rooms")
    public String getRoom() throws IOException {
        Rooms rooms = session.getRooms();
        return rooms.toString();
    }
    
    @GetMapping("/weeklyTimetable/room/{id}")
    public String getRoomById(@PathVariable int id) throws IOException {
        WeeklyTimetable weeklyTimetableFromClassId = session.getWeeklyTimetable(LocalDate.now(), UntisUtils.ElementType.ROOM, id);
        return weeklyTimetableFromClassId.toString();
    }
    
    @GetMapping("/weeklyTimetable/class/{id}")
    public String getClassById(@PathVariable int id) throws IOException {
        WeeklyTimetable weeklyTimetableFromClassId = session.getWeeklyTimetableFromClassId(LocalDate.now(), id);
        return weeklyTimetableFromClassId.toString();
    }
    
    @GetMapping("/teachers")
    public String getTeachers() throws IOException {
        Teachers teachers = session.getTeachers();
        return teachers.toString();
    }
}
