package at.ac.tgm.mapper;

import at.ac.tgm.dto.MeilensteinDTO;
import at.ac.tgm.entity.Meilenstein;

public final class MeilensteinMapper {

    private MeilensteinMapper() {}

    /**
     * Wandelt ein Meilenstein-Objekt in ein entsprechendes MeilensteinDTO um.
     *
     * @param m Das zu konvertierende Meilenstein-Objekt.
     * @return Das resultierende MeilensteinDTO.
     */
    public static MeilensteinDTO toDTO(Meilenstein m) {
        if (m == null) return null;
        MeilensteinDTO dto = new MeilensteinDTO();
        dto.setMeilensteinId(m.getMeilensteinId());
        dto.setName(m.getName());
        dto.setStatus(m.getStatus());
        if (m.getDiplomarbeit() != null) {
            dto.setProjektId(m.getDiplomarbeit().getProjektId());
        }
        return dto;
    }
}
