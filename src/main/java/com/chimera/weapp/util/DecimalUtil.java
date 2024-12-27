package com.chimera.weapp.util;

public class DecimalUtil {
    public static void main(String[] args) {
        String number = "1";
        String shiftedNumber = convertYuanToFen(number);
        System.out.println(shiftedNumber);
    }

    public static String convertYuanToFen(String integerString) {
        if (integerString == null || integerString.isEmpty()) {
            throw new IllegalArgumentException("输入字符串不能为空");
        }

        // 检查是否为有效数字
        if (!integerString.matches("\\d+")) {
            throw new IllegalArgumentException("输入字符串必须是自然数");
        }

        // 补齐两位
        if (integerString.length() == 1) {
            return "0.0" + integerString;
        } else if (integerString.length() == 2) {
            return "0." + integerString;
        } else {
            // 插入小数点
            int len = integerString.length();
            return integerString.substring(0, len - 2) + "." + integerString.substring(len - 2);
        }
    }

}