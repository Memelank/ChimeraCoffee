package com.chimera.weapp.util;

import com.alibaba.fastjson2.JSON;

import java.util.HashMap;

public class JSONUtils {
    public static String buildResponseBody(String msg, Object data) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg", msg);
        map.put("data", data);
        return JSON.toJSONString(map);
    }
}
