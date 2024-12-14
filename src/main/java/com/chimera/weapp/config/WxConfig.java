package com.chimera.weapp.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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


    /**
     * 构建通用的 RSAAutoCertificateConfig.Builder，用于减少重复代码
     */
    private RSAAutoCertificateConfig.Builder getCommonBuilder() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key);
    }

    /**
     * 创建 NotificationConfig Bean
     */
    @Bean
    public NotificationConfig wechatPayNotificationConfig() {
        return getCommonBuilder().build();
    }

    /**
     * 创建 Config Bean
     */
    @Bean
    @Primary
    public Config wechatPayConfig() {
        return getCommonBuilder().build();
    }
}
