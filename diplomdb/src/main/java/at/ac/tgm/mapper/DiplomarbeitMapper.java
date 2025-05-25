package at.ac.tgm.mapper;

import at.ac.tgm.dto.DiplomarbeitResponseDTO;
import at.ac.tgm.entity.Diplomarbeit;

public final class DiplomarbeitMapper {

    private DiplomarbeitMapper() {}

    /**
     * Wandelt ein Diplomarbeit-Objekt in ein entsprechendes Response-Datentransferobjekt um.
     */
    public static DiplomarbeitResponseDTO toResponseDTO(Diplomarbeit d) {
        if (d == null) return null;
        DiplomarbeitResponseDTO dto = new DiplomarbeitResponseDTO();
        dto.setProjektId(d.getProjektId());
        dto.setTitel(d.getTitel());
        dto.setBeschreibung(d.getBeschreibung());
        dto.setStatus(d.getStatus());
        dto.setStartdatum(d.getStartdatum());
        dto.setEnddatum(d.getEnddatum());
        dto.setAblehnungsgrund(d.getAblehnungsgrund());
        dto.setBetreuerSamAccountName(d.getBetreuerSamAccountName());
        dto.setMitarbeiterSamAccountNames(d.getMitarbeiterSamAccountNames());
        return dto;
    }
}
