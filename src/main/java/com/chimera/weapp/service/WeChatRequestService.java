package com.chimera.weapp.service;

import com.alibaba.fastjson2.JSONObject;
import com.chimera.weapp.dto.*;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.util.ThreadLocalUtil;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
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
import java.util.Objects;


@Service
public class WeChatRequestService {
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
    @Value("${wx-mini-program.state")
    private String miniprogram_state;
    @Autowired
    private OrderService orderService;

    private static final String ACCESS_TOKEN = "access_token";

    // 公共请求方法，抽取相同逻辑
    private String sendHttpRequest(URIBuilder uriBuilder, ClassicHttpRequest httpRequest) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHost target = new HttpHost(uriBuilder.getScheme(), uriBuilder.getHost());
            return httpClient.execute(target, httpRequest, (ClassicHttpResponse response) ->
                    EntityUtils.toString(response.getEntity()));
        }
    }

    public JSONObject code2session(String code) throws IOException, URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/sns/jscode2session");
        uriBuilder.addParameter("appid", appid)
                .addParameter("secret", secret)
                .addParameter("js_code", code);

        ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(uriBuilder.build()).build();
        String body = sendHttpRequest(uriBuilder, httpRequest);
        return JSONObject.parse(body);
    }

    public PrePaidDTO jsapiTransaction(Order save) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi");

        ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(uriBuilder.build())
                .setEntity(buildRequestBody(save))
                .build();

        String body = sendHttpRequest(uriBuilder, httpRequest);
        return JSONObject.parseObject(body, PrePaidDTO.class);
    }

    private String grant(String grant_type) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/cgi-bin/token");
        uriBuilder.addParameter("grant_type", grant_type)
                .addParameter("appid", appid)
                .addParameter("secret", secret);
        ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(uriBuilder.build()).build();
        String body = sendHttpRequest(uriBuilder, httpRequest);
        JSONObject jsonObject = JSONObject.parseObject(body);
        String errcode = jsonObject.getString("errcode");
        if (Objects.nonNull(errcode)) {
            throw new RuntimeException("调用https://api.weixin.qq.com/cgi-bin/token。grant失败：" + body);
        }
        return body;
    }

    public WxStudentCheckDTO checkStudentIdentity(WxCheckStudentIdentityApiParams apiParams) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/intp/quickcheckstudentidentity");
        String grant = grant(ACCESS_TOKEN);
        WxGrantAccessTokenDTO wxGrantAccessTokenDTO = JSONObject.parseObject(grant, WxGrantAccessTokenDTO.class);
        uriBuilder.addParameter(ACCESS_TOKEN, wxGrantAccessTokenDTO.getAccess_token());
        ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(uriBuilder.build())
                .setEntity(JSONObject.toJSONString(apiParams))
                .build();
        String body = sendHttpRequest(uriBuilder, httpRequest);
        return JSONObject.parseObject(body, WxStudentCheckDTO.class);
    }


    @Builder
    public static class WxCheckStudentIdentityApiParams {
        @NotNull
        String openid;
        @NotNull
        String wx_studentcheck_code;
    }

    public <T> void subscribeSend(T content, String page, String template_id, String touser) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/cgi-bin/message/subscribe/send");
        String grant = grant(ACCESS_TOKEN);
        uriBuilder.addParameter(ACCESS_TOKEN, grant);
        WxSubscribeSendApiParams.WxSubscribeSendApiParamsBuilder builder = WxSubscribeSendApiParams.builder()
                .template_id(template_id)
                .touser(touser)
                .data(JSONObject.toJSONString(content))
                .miniprogram_state(miniprogram_state)
                .lang("zh_CN");
        if (Objects.nonNull(page) || !template_id.isBlank()) {
            builder.page(page);
        }

        ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(uriBuilder.build())
                .setEntity(JSONObject.toJSONString(builder.build()))
                .build();
        String body = sendHttpRequest(uriBuilder, httpRequest);
        JSONObject jsonObject = JSONObject.parseObject(body);
        String errcode = jsonObject.getString("errcode");
        if (Objects.nonNull(errcode)) {
            throw new RuntimeException("调用https://api.weixin.qq.com/cgi-bin/message/subscribe/send。subscribeSend失败：" + body);
        }
    }

    @Builder
    static class WxSubscribeSendApiParams {
        @NotNull
        String template_id;
        String page;
        @NotNull
        String touser;
        @NotNull
        String data;
        @NotNull
        String miniprogram_state;
        @NotNull
        String lang;
    }

    private String buildRequestBody(Order save) {
        ObjectId orderId = save.getId();
        String openid = ThreadLocalUtil.get(ThreadLocalUtil.USER_DTO, UserDTO.class).getOpenid();
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
