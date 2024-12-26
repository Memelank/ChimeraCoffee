package com.chimera.weapp.service;

import com.alibaba.fastjson2.JSONObject;
import com.chimera.weapp.apiparams.SupplyNoticeApiParams;
import com.chimera.weapp.dto.*;
import com.chimera.weapp.util.CaffeineCacheUtil;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@Slf4j
@Service
public class WeChatRequestService {
    @Value("${wx-mini-program.appid}")
    private String appid;
    @Value("${wx-mini-program.secret}")
    private String secret;
    @Value("${wx-mini-program.mchid}")
    private String mchid;
    @Value("${wx-mini-program.state")
    private String miniprogram_state;

    private static final String ACCESS_TOKEN = "access_token";

    private static final String GRANT_TYPE = "client_credential";

    // 公共请求方法，抽取相同逻辑
    private String sendHttpRequest(URIBuilder uriBuilder, ClassicHttpRequest httpRequest) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHost target = new HttpHost(uriBuilder.getScheme(), uriBuilder.getHost());
            return httpClient.execute(target, httpRequest, (ClassicHttpResponse response) ->
                    EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * 2.调用 auth.code2Session 接口，换取 用户唯一标识 OpenID 、 用户在微信开放平台账号下的唯一标识UnionID（若当前小程序已绑定到微信开放平台账号） 和 会话密钥 session_key
     * <a href="https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html">链接</a>
     *
     * @param code 1.调用 wx.login() 获取 临时登录凭证code ，并回传到开发者服务器。
     */
    public JSONObject code2session(String code) throws IOException, URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/sns/jscode2session");
        uriBuilder.addParameter("appid", appid)
                .addParameter("secret", secret)
                .addParameter("js_code", code);

        ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(uriBuilder.build()).build();
        String body = sendHttpRequest(uriBuilder, httpRequest);
        return JSONObject.parse(body);
    }

    /**
     * 获取接口调用凭据
     * <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getAccessToken.html">链接</a>
     */
    private String getAccessToken() throws URISyntaxException, IOException {
        Optional<Object> optional = CaffeineCacheUtil.get("access_token");
        if (optional.isPresent()) {
            return (String) optional.get();
        }

        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/cgi-bin/stable_token");
        StableTokenApiParams apiParams = StableTokenApiParams.builder().grant_type(WeChatRequestService.GRANT_TYPE).appid(appid).secret(secret).build();
        ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(uriBuilder.build()).setEntity(JSONObject.toJSONString(apiParams)).build();
        String body = sendHttpRequest(uriBuilder, httpRequest);
        JSONObject jsonObject = JSONObject.parseObject(body);
        String accessToken = jsonObject.getString("access_token");
        if (Objects.nonNull(accessToken)) {
            Long expiresIn = jsonObject.getLong("expires_in");
            CaffeineCacheUtil.put("access_token", accessToken, expiresIn * 1000);
            return accessToken;
        } else {
            throw new RuntimeException(String.format("调用 https://api.weixin.qq.com/cgi-bin/token 失败,body: %s", body));
        }
    }

    @Builder
    @Data
    public static class StableTokenApiParams {
        String grant_type;
        String appid;
        String secret;
        String force_refresh;
    }

    /**
     * 快速获取学生身份API
     * <a href="https://developers.weixin.qq.com/miniprogram/dev/platform-capabilities/industry/student.html">链接</a>
     */
    public WxStudentCheckDTO checkStudentIdentity(WxCheckStudentIdentityApiParams apiParams) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/intp/quickcheckstudentidentity");
        String grant = getAccessToken();
        uriBuilder.addParameter(ACCESS_TOKEN, grant);
        ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(uriBuilder.build())
                .setEntity(JSONObject.toJSONString(apiParams))
                .build();
        String body = sendHttpRequest(uriBuilder, httpRequest);
        WxStudentCheckDTO wxStudentCheckDTO = JSONObject.parseObject(body, WxStudentCheckDTO.class);
        log.info("获取学生身份API响应:{},转成dto后的结果:{}", body, wxStudentCheckDTO);
        return wxStudentCheckDTO;
    }


    @Builder
    @Data
    public static class WxCheckStudentIdentityApiParams {
        @NotNull
        String openid;
        @NotNull
        String wx_studentcheck_code;
    }


    /**
     * 发送订阅消息 <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html">链接</a>
     *
     * @param data        内容
     * @param page        跳转页面
     * @param template_id 模板id
     * @param touser      接收者（用户）id
     */
    public void subscribeSend(Map<String, Map<String, String>> data, String page, String template_id, String touser) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/cgi-bin/message/subscribe/send");
        String grant = getAccessToken();
        uriBuilder.addParameter(ACCESS_TOKEN, grant);
        WxSubscribeSendApiParams.WxSubscribeSendApiParamsBuilder builder = WxSubscribeSendApiParams.builder()
                .template_id(template_id)
                .touser(touser)
                .data(data)
                .miniprogram_state(miniprogram_state)
                .lang("zh_CN");
        if (Objects.nonNull(page) || !template_id.isBlank()) {
            builder.page(page);
        }

        String jsonString = JSONObject.toJSONString(builder.build());
        log.info("发送订阅消息请求的body是:{}", jsonString);
        ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(uriBuilder.build())
                .setEntity(jsonString)
                .build();
        String body = sendHttpRequest(uriBuilder, httpRequest);
        log.info("发送订阅消息响应的body是:{}", jsonString);
        JSONObject jsonObject = JSONObject.parseObject(body);
        String errcode = jsonObject.getString("errcode");
        if (Objects.nonNull(errcode)) {
            throw new RuntimeException("调用https://api.weixin.qq.com/cgi-bin/message/subscribe/send。subscribeSend失败：" + body);
        }
    }

    @Builder
    @Data
    static class WxSubscribeSendApiParams {
        @NotNull
        String template_id;
        String page;
        @NotNull
        String touser;
        @NotNull
        Map<String, Map<String, String>> data;
        @NotNull
        String miniprogram_state;
        @NotNull
        String lang;
    }

}
