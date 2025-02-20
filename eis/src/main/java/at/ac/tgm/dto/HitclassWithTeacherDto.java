package at.ac.tgm.dto;

import java.util.List;

public class HitclassWithTeacherDto {
    private Long id;
    private String name;
    private String klassenvorstand; // Name des Klassenvorstands
    private List<TeacherDto> teachers; // Liste der Lehrer

    // Konstruktor
    public HitclassWithTeacherDto(Long id, String name, String klassenvorstand, List<TeacherDto> teachers) {
        this.id = id;
        this.name = name;
        this.klassenvorstand = klassenvorstand;
        this.teachers = teachers;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getKlassenvorstand() {
        return klassenvorstand;
    }

    public List<TeacherDto> getTeachers() {
        return teachers;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKlassenvorstand(String klassenvorstand) {
        this.klassenvorstand = klassenvorstand;
    }

    public void setTeachers(List<TeacherDto> teachers) {
        this.teachers = teachers;
    }
}