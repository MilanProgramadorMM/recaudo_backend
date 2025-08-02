package com.recaudo.api.infrastructure.helper.util;


import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Utils {

    static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static Boolean validatePassword(String originalPassword, String hashPassword) {
        return BCrypt.checkpw(originalPassword, hashPassword);
    }

    public static String encodePassword(String password){
        return passwordEncoder.encode(password);
    }

    public static LocalDateTime localDateTimeFormatter(Date dateToConvert){
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static Boolean isExpiredCode(LocalDateTime expiredAt){
        return !expiredAt.isAfter(LocalDateTime.now());
    }
}
