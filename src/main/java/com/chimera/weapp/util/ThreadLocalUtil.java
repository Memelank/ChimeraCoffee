package com.chimera.weapp.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalUtil {

    // 使用ThreadLocal存储一个Map, 每个线程都有一个独立的Map
    private static final ThreadLocal<Map<String, Object>> threadLocalMap = ThreadLocal.withInitial(HashMap::new);

    public static final String USER_DTO="userDTO";
    public static final String CLAIMS="claims";

    // 设置值到ThreadLocal中的Map
    public static <T> void set(String key, T value) {
        threadLocalMap.get().put(key, value);
    }

    // 从ThreadLocal中的Map获取值
    public static <T> T get(String key) {
        return (T) threadLocalMap.get().get(key);
    }

    // 删除ThreadLocal中的某个key对应的值
    public static void remove(String key) {
        threadLocalMap.get().remove(key);
    }

    // 清空ThreadLocal
    public static void clear() {
        threadLocalMap.remove();
    }
}
