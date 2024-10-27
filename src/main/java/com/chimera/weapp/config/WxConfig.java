package com.chimera.weapp.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.auth.Credential;
import com.wechat.pay.java.core.auth.Validator;
import com.wechat.pay.java.core.cipher.*;
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
//        return new RSAAutoCertificateConfig.Builder()
//                .merchantId(merchantId)
//                .privateKeyFromPath(privateKeyPath)
//                .merchantSerialNumber(merchantSerialNumber)
//                .apiV3Key(apiV3Key)
//                .build();
        return new NotificationConfig() {
            @Override
            public String getSignType() {
                return "";
            }

            @Override
            public String getCipherType() {
                return "";
            }

            @Override
            public Verifier createVerifier() {
                return null;
            }

            @Override
            public AeadCipher createAeadCipher() {
                return null;
            }
        };
    }

    @Bean
    public Config createConfig() {//todo 成为商户后才能有这四个值
//        return new RSAAutoCertificateConfig.Builder()
//                .merchantId(merchantId)
//                .privateKeyFromPath(privateKeyPath)
//                .merchantSerialNumber(merchantSerialNumber)
//                .apiV3Key(apiV3Key)
//                .build();
        return new Config() {
            @Override
            public PrivacyEncryptor createEncryptor() {
                return null;
            }

            @Override
            public PrivacyDecryptor createDecryptor() {
                return null;
            }

            @Override
            public Credential createCredential() {
                return null;
            }

            @Override
            public Validator createValidator() {
                return null;
            }

            @Override
            public Signer createSigner() {
                return null;
            }
        };
    }
}
