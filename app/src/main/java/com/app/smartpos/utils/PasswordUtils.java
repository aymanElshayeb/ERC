package com.app.smartpos.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {


    public static String generateHashedPass(String pass) {
        // hash a plaintext password using the typical log rounds (10)
        return BCrypt.hashpw(pass, BCrypt.gensalt());
    }

    public static boolean isValid(String clearTextPassword, String hashedPass) {
        // returns true if password matches hash
        return BCrypt.checkpw(clearTextPassword, hashedPass);
    }

}
