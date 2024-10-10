package at.ac.tgm.ad.util;

import javax.naming.Name;

public class Util {
    public static String getCnFromName(Name name) {
        String[] parts = name.get(name.size() - 1).split("=");
        if (parts[0].equalsIgnoreCase("cn") && parts[1] != null) {
            return parts[1];
        } else {
            throw new IllegalArgumentException("Last part of the name is not a CN");
        }
    }
}
