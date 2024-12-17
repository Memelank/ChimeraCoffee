package com.chimera.weapp.apiparams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class NoticeApiParams {
    @Override
    public String toString() {
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

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
