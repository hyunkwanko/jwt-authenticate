package com.taron.authenticate.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeUtil {

    public static final String DATE_PATTERN_TYPE1 = "yyyy.MM.dd HH:mm";
    public static final String DATE_PATTERN_TYPE2 = "yyyyMMdd";
    public static final String DATE_PATTERN_TYPE3 = "yyyy-MM-dd";

    public static String getStringToLocalDateTime(LocalDateTime localDateTime, String datePatternType) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePatternType);
        return localDateTime.format(dateTimeFormatter);
    }
}
