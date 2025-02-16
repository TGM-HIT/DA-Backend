package at.ac.tgm.ad.util;

public class EntryBase {
    public static final String GROUP = "OU=Groups";
    public static final String PEOPLE = "OU=People";
    public static final String LEHRER = "OU=Lehrer,OU=People";
    public static final String SCHUELER = "OU=Schueler,OU=People";
    public static final String SCHUELER_HIT = "OU=HIT," + SCHUELER;
}
