package com.chat.base.net;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static UserService instance;

    private UserService() {}

    public static UserService getInstance() {
        if (instance == null) {
            synchronized (UserService.class) {
                if (instance == null) {
                    instance = new UserService();
                }
            }
        }
        return instance;
    }

    // 定义回调接口
    public interface IHttpRequestCallback {
        void onSuccess(String data);
        void onError(int code, String msg);
    }

    // 获取用户余额的方法
    public void getUserAmount(String uid, IHttpRequestCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("uid", uid);
        HttpRequest.getInstance()
            .get("http://152.42.170.13:8787/V2/getUserInfo", 
                params, 
                callback);
    }
} 