package com.chimera.weapp.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // 哈希密码
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // 验证密码
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
