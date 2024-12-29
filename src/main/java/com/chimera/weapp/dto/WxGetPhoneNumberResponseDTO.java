package com.chimera.weapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxGetPhoneNumberResponseDTO {
    private int errcode;
    private String errmsg;
    private PhoneInfo phone_info;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PhoneInfo {
        private String phoneNumber;
        private String purePhoneNumber;
        private String countryCode;
        private WaterMark watermark;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WaterMark {
        private int timestamp;
        private String appid;
    }
}
