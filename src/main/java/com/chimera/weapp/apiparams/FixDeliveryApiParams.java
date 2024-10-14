package com.chimera.weapp.apiparams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class FixDeliveryApiParams {
    @Schema(description = "取货时间")
    private String time9;
    @Schema(description = "收货地址")
    private String thing17;
    @Schema(description = "联系人手机号")
    private String phone_number16;
    @Schema(description = "温馨提示")
    private String thing5;
    @Schema(description = "取餐码")
    private String character_string1;
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
        } catch (IllegalAccessException | JsonProcessingException e) {
            log.error("DineInOrTakeOutApiParams toString失败了", e);
        }

        return "{}";  // 返回空 JSON
    }
}
