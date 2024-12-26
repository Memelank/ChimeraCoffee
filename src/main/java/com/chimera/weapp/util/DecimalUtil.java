package com.chimera.weapp.util;

public class DecimalUtil {
    public static void main(String[] args) {
        String number = "12345"; // 假设这是你的整数形式的字符串
        String shiftedNumber = shiftDecimalPointLeft(number, 2);
        System.out.println(shiftedNumber); // 输出结果
    }

    public static String shiftDecimalPointLeft(String number, int places) {
        // 检查是否有足够的数字来移动小数点
        if (number.length() <= places) {
            return "0.0"; // 如果没有足够的数字，返回0.0
        }

        // 将字符串分割为整数部分和小数部分
        String integerPart = number.substring(0, number.length() - places);
        String decimalPart = number.substring(number.length() - places);

        // 如果整数部分以0开头，去掉这些0
        while (integerPart.startsWith("0") && integerPart.length() > 1) {
            integerPart = integerPart.substring(1);
        }

        // 如果整数部分为空，说明原字符串全是0，返回0.
        if (integerPart.isEmpty()) {
            return "0.";
        }

        // 将整数部分和小数部分用小数点连接起来
        return integerPart + "." + decimalPart;
    }
}