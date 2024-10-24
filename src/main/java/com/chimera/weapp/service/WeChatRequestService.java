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

    private static final String GRANT_TYPE = "client_credential";

    // 公共请求方法，抽取相同逻辑
    private String sendHttpRequest(URIBuilder uriBuilder, ClassicHttpRequest httpRequest) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHost target = new HttpHost(uriBuilder.getScheme(), uriBuilder.getHost());
            return httpClient.execute(target, httpRequest, (ClassicHttpResponse response) ->
                    EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * 2.调用 auth.code2Session 接口，换取 用户唯一标识 OpenID 、 用户在微信开放平台账号下的唯一标识UnionID（若当前小程序已绑定到微信开放平台账号） 和 会话密钥 session_key
     * <a href="https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html">链接</a>
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
     * JSAPI下单
     * 商户系统先调用该接口在微信支付服务后台生成预支付交易单，返回正确的预支付交易会话标识后再按Native、JSAPI、APP等不同场景生成交易串调起支付。
     * <a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_1.shtml">链接</a>
     *
     */
    public PrePaidDTO jsapiTransaction(Order save) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi");

        ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(uriBuilder.build())
                .setEntity(orderService.buildJSAPIRequestBody(save, appid, mchid, notifyURL))
                .build();

        String body = sendHttpRequest(uriBuilder, httpRequest);
        return JSONObject.parseObject(body, PrePaidDTO.class);
    }

    /**
     * 获取接口调用凭据
     * <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getAccessToken.html">链接</a>
     */
    private String getAccessToken() throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/cgi-bin/token");
        uriBuilder.addParameter("grant_type", WeChatRequestService.GRANT_TYPE)
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

    /**
     * 快速获取学生身份API
     * <a href="https://developers.weixin.qq.com/miniprogram/dev/platform-capabilities/industry/student.html">链接</a>
     */
    public WxStudentCheckDTO checkStudentIdentity(WxCheckStudentIdentityApiParams apiParams) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/intp/quickcheckstudentidentity");
        String grant = getAccessToken();
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


    /**
     * 发送订阅消息 <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html">链接</a>
     *
     * @param content     内容
     * @param page        跳转页面
     * @param template_id 模板id
     * @param touser      接收者（用户）id
     * @param <T>         内容类型
     */
    public <T> void subscribeSend(T content, String page, String template_id, String touser) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder("https://api.weixin.qq.com/cgi-bin/message/subscribe/send");
        String grant = getAccessToken();
        uriBuilder.addParameter(ACCESS_TOKEN, grant);
        WxSubscribeSendApiParams.WxSubscribeSendApiParamsBuilder builder = WxSubscribeSendApiParams.builder()
                .template_id(template_id)
                .touser(touser)
                .data(content.toString())
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

}
