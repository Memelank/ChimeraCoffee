package com.chimera.weapp.service;

import com.alibaba.fastjson2.JSONObject;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.UserRepository;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.net.URIBuilder;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


@Service
public class WeChatService {
    @Autowired
    private UserRepository userRepository;
    @Value("${wx-mini-program.appid}")
    private String appid;
    @Value("${wx-mini-program.secret}")
    private String secret;
    @Value("${wx-mini-program.notify_url}")
    private String notifyURL;
    @Value("${wx-mini-program.mchid}")
    private String mchid;
    @Autowired
    private OrderService orderService;

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

    public JSONObject jsapiTransaction(Order save) throws URISyntaxException, IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {//todo 和code2session的逻辑相似，应抽取函数
            URIBuilder uriBuilder = new URIBuilder("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi");

            HttpHost target = new HttpHost(uriBuilder.getScheme(), uriBuilder.getHost());
            ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(uriBuilder.build()).setEntity(buildRequestBody(save)).build();
            String body = httpClient.execute(target, httpRequest, classicHttpResponse -> EntityUtils.toString(classicHttpResponse.getEntity()));
            return JSONObject.parseObject(body);
        }
    }

    private String buildRequestBody(Order save) {
        //todo 因为根据用户id取openid是频繁操作，应该从requestAttribute获取openid，但是暂时先不改SecurityAspect那边了
        User customer = userRepository.findById(save.getUserId()).orElseThrow();
        ObjectId orderId = save.getId();
        String openid = customer.getOpenid();
        String description = orderService.getDescription(save);
        Map<String, Object> map = new HashMap<>();
        map.put("appid", appid);
        map.put("mchid", mchid);
        map.put("description", description);
        map.put("out_trade_no", orderId.toHexString());
        map.put("notify_url", notifyURL);
        map.put("amount", String.format("{\"total\":%d,\"currency\":\"CNY\"}", save.getTotalPrice()));
        map.put("payer", String.format("{\"openid\":\"%s\"}", openid));
        return JSONObject.toJSONString(map);
    }
}
