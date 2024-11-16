package at.ac.tgm;

public class UserRoles {
    public static final String USER = "USER";
    public static final String SCHUELER = "SCHUELER";
    public static final String LEHRER = "LEHRER";
    
    public static String getRoleFromDn(String dn) {
        String role = UserRoles.USER;
        if (dn.contains("OU=Lehrer")) {
            role = UserRoles.LEHRER;
        }
        if (dn.contains("OU=Schueler")) {
            role = UserRoles.SCHUELER;
        }
        return role;
    }
}