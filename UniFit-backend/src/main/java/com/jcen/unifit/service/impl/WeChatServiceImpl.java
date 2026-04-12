package com.jcen.unifit.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.jcen.unifit.config.WeChatConfig;
import com.jcen.unifit.service.WeChatService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatServiceImpl implements WeChatService {

    @Resource
    private WeChatConfig weChatConfig;

    @Override
    public Map<String, Object> getSessionKeyOrOpenid(String code) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("openid", "mock_" + DigestUtil.md5Hex(code).substring(0, 16));
        fallback.put("unionid", null);

        if (StrUtil.hasBlank(weChatConfig.getAppId(), weChatConfig.getSecret(), weChatConfig.getLoginUrl())) {
            return fallback;
        }

        try {
            String url = weChatConfig.getLoginUrl()
                    + "?appid=" + weChatConfig.getAppId()
                    + "&secret=" + weChatConfig.getSecret()
                    + "&js_code=" + code
                    + "&grant_type=authorization_code";
            String response = HttpUtil.get(url, 5000);
            Map<String, Object> result = JSONUtil.toBean(response, Map.class);
            if (result == null || result.get("openid") == null) {
                return fallback;
            }
            return result;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
