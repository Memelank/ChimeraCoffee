package com.chimera.weapp.apiparams;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class NoticeApiParams {
    /***
     * 构造这个沟槽data
     * 发送订阅消息 <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html#%E8%AF%B7%E6%B1%82%E6%95%B0%E6%8D%AE%E7%A4%BA%E4%BE%8B">链接</a>
     */
    public Map<String, Map<String, String>> buildGouCaoData() {
        Map<String, Map<String, String>> jsonMap = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();

        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                String fieldValue = (String) field.get(this);

                Map<String, String> valueMap = new HashMap<>();
                valueMap.put("value", fieldValue);

                jsonMap.put(fieldName, valueMap);
            }

            return jsonMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
