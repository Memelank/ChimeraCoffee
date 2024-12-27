package com.chimera.weapp.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeRangeChecker {

    public static boolean isNowInTimeRange(String startTime, String endTime) {
        try {
            // 格式化器，假设输入格式为 "HH:mm"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse(startTime, formatter);
            LocalTime end = LocalTime.parse(endTime, formatter);

            if (start.isBefore(end)) {
                // 时间段在同一天
                return now.isAfter(start) && now.isBefore(end);
            } else {
                // 时间段跨越午夜
                return now.isAfter(start) || now.isBefore(end);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("时间格式不正确，必须为 HH:mm", e);
        }
    }

    public static void main(String[] args) {
        System.out.println(isNowInTimeRange("22:00", "06:00")); // 示例输出
        System.out.println(isNowInTimeRange("08:00", "20:00")); // 示例输出
    }
}
