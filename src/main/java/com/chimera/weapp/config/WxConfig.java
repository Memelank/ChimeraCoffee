package com.chimera.weapp.config;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WxConfig {
    @Value("${wx-mini-program.mchid}")
    private String merchantId;
    @Value("${wx-mini-program.private-key-path}")
    private String privateKeyPath;
    @Value("${wx-mini-program.merchant-serial-number}")
    private String merchantSerialNumber;
    @Value("${wx-mini-program.api-v3-key}")
    private String apiV3Key;
    @Bean
    public NotificationConfig build() {//todo 成为商户后才能有这四个值
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();
    }
}
