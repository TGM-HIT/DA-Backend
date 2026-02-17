package at.ac.tgm.ad;

import java.util.Arrays;

public class Roles {
    public final static String ADMIN = "ROLE_ADMIN";
    public final static String TEACHER = "ROLE_TEACHER";
    public final static String STUDENT = "ROLE_STUDENT";
    
    public static boolean contains(String role) {
        for (String c : Arrays.asList(ADMIN, TEACHER, STUDENT)) {
            if (c.equalsIgnoreCase(role) || c.equalsIgnoreCase("ROLE_" + role)) {
                return true;
            }
        }
        return false;
    }
}
