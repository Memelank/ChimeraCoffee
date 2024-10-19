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
public class SupplyNoticeApiParams {
    @Schema(description = "订单状态")
    private String phrase19;
    @Schema(description = "温馨提示")
    private String thing11;
    @Schema(description = "门店名称")
    private String thing2;
    @Schema(description = "取餐地址")
    private String thing7;
    @Schema(description = "取餐号码")
    private String character_string4;

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
