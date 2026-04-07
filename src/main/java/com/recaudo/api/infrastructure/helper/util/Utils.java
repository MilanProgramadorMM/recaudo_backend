package com.recaudo.api.infrastructure.helper.util;


import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

    public static Date atStartOfDay(Date date) {
        TimeZone tz = TimeZone.getTimeZone("America/Bogota");
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        Calendar result = Calendar.getInstance(tz);
        result.clear();
        result.set(year, month, day, 0, 0, 0);
        return result.getTime();
    }

    public static Date atStartOfNextDay(Date date) {
        TimeZone tz = TimeZone.getTimeZone("America/Bogota");
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        Calendar result = Calendar.getInstance(tz);
        result.clear();
        result.set(year, month, day, 0, 0, 0);
        result.add(Calendar.DAY_OF_MONTH, 1);
        return result.getTime();
    }
}
