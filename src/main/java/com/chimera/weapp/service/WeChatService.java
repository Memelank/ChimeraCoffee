package com.chimera.weapp.service;

import com.alibaba.fastjson2.JSONObject;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;

@Service
public class WeChatService {
    @Value("${wx-mini-program.appid}")
    private String appid;
    @Value("${wx-mini-program.secret}")
    private String secret;

    public JSONObject code2session(String code) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/sns/jscode2session");

            uriBuilder.addParameter("appid", appid)
                    .addParameter("secret", secret)
                    .addParameter("js_code", code);
            HttpHost target = new HttpHost(uriBuilder.getScheme(), uriBuilder.getHost());

            ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(uriBuilder.build()).build();
            String body = httpClient.execute(target, httpRequest, classicHttpResponse -> EntityUtils.toString(classicHttpResponse.getEntity()));
            return JSONObject.parse(body);
        }
    }
}
