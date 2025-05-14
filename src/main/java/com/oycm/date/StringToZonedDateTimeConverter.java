package com.oycm.date;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * @author ouyangcm
 * create 2025/4/17 15:24
 */
public class StringToZonedDateTimeConverter {

    /**
     *
     * @param source 时间戳或 2007-12-03T10:15:30+01:00[Europe/Paris] 格式时间
     * @return
     */
    public static ZonedDateTime convert(String source) {
        ZonedDateTime dateTime;
        try {
            long epoch = Long.parseLong(source);

            dateTime = Instant.ofEpochMilli(epoch).atOffset(ZoneOffset.ofTotalSeconds(0)).toZonedDateTime();

        } catch (NumberFormatException e) {
            // 默认解析
            dateTime = ZonedDateTime.parse(source);
        }

        return dateTime;
    }

    public static void main(String[] args) {
        ZonedDateTime convert = convert("2007-12-03T10:15:30+01:00[Asia/Shanghai]");

        //convert = convert(System.currentTimeMillis() + "");

        System.out.println(ZoneId.systemDefault());

        System.out.println(convert.toString());
    }

}
