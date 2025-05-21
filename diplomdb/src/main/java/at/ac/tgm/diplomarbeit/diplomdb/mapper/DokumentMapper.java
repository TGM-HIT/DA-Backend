package at.ac.tgm.diplomarbeit.diplomdb.mapper;

import at.ac.tgm.diplomarbeit.diplomdb.dto.DokumentDTO;
import at.ac.tgm.diplomarbeit.diplomdb.entity.Dokument;

public final class DokumentMapper {

    private DokumentMapper() {}

    /**
     * Wandelt ein Dokument-Objekt in ein DokumentDTO um.
     *
     * @param d Das zu konvertierende Dokument.
     * @return Das resultierende DokumentDTO.
     */
    public static DokumentDTO toDTO(Dokument d) {
        if (d == null) return null;
        DokumentDTO dto = new DokumentDTO();
        dto.setDokumentId(d.getDokumentId());
        dto.setTitel(d.getTitel());
        dto.setBeschreibung(d.getBeschreibung());
        dto.setTyp(d.getTyp());
        dto.setHochladungsdatum(d.getHochladungsdatum());
        dto.setDatum(d.getDatum());
        dto.setDateiname(d.getDateiname());
        dto.setErstellerSamAccountName(d.getErstellerSamAccountName());
        dto.setBewertungProzent(d.getBewertungProzent());
        dto.setBewertetDurchSamAccountName(d.getBewertetDurchSamAccountName());
        dto.setBewertungsKommentar(d.getBewertungsKommentar());

        if (d.getDiplomarbeit() != null) {
            dto.setDiplomarbeitId(d.getDiplomarbeit().getProjektId());
            dto.setDiplomarbeitTitel(d.getDiplomarbeit().getTitel());
        }
        return dto;
    }
}
