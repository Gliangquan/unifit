package com.jcen.unifit.service;

import java.util.Map;

public interface WeChatService {

    Map<String, Object> getSessionKeyOrOpenid(String code);
}
