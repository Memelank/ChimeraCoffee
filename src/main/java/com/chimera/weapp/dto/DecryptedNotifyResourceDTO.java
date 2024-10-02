package com.chimera.weapp.dto;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecryptedNotifyResourceDTO {
    private String appid;
    private String mchid;
    private String out_trade_no;
    private String transaction_id;
    private String trade_type;
    private String trade_state;
    private String trade_state_desc;
    private String bank_type;
    private String attach;
    private String success_time;
    private JSONObject payer;
    private JSONObject amount;
    private JSONObject scene_info;
    private JSONArray array;
}
