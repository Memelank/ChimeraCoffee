package com.chimera.weapp.util;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
public class DateUtil {
    // 定义日期格式
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static String formatDate(Date date) {
        // 设置为东八区（中国标准时间）
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 格式化日期
        return sdf.format(date);
    }

    public static void main(String[] args) {
        System.out.println(formatDate(new Date()));
    }
}
