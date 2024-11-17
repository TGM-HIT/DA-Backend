package at.ac.tgm;

public class UserRoles {
    public static final String USER = "USER"; // Any logged-in user
    public static final String SCHUELER = "SCHUELER";
    public static final String LEHRER = "LEHRER";
    //public static final String ELTERN = "ELTERN";
    
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