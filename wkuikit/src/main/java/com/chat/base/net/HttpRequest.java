package com.chat.base.net;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.chat.base.config.WKConfig;
import com.chat.base.utils.WKLogUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpRequest {
    private static HttpRequest instance;
    private final OkHttpClient client;
    private final Handler handler;

    private HttpRequest() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        handler = new Handler(Looper.getMainLooper());
    }

    public static HttpRequest getInstance() {
        if (instance == null) {
            synchronized (HttpRequest.class) {
                if (instance == null) {
                    instance = new HttpRequest();
                }
            }
        }
        return instance;
    }

    public void get(String url, Map<String, String> params, UserService.IHttpRequestCallback callback) {
        // 构建带参数的URL
        StringBuilder urlBuilder = new StringBuilder(url);
        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }

        Request request = new Request.Builder()
                .url(urlBuilder.toString())
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> callback.onError(-1, e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    if (response.isSuccessful()) {
                        handler.post(() -> callback.onSuccess(responseData));
                    } else {
                        handler.post(() -> callback.onError(response.code(), responseData));
                    }
                } catch (Exception e) {
                    handler.post(() -> callback.onError(-1, e.getMessage()));
                }
            }
        });
    }
} 