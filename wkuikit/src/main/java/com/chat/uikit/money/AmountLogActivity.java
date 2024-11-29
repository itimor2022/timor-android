package com.chat.uikit.money;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKConfig;
import com.chat.base.net.HttpRequest;
import com.chat.base.net.UserService;
import com.chat.uikit.databinding.ActAmountLogBinding;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AmountLogActivity extends WKBaseActivity<ActAmountLogBinding> {

    @Override
    protected ActAmountLogBinding getViewBinding() {
        return ActAmountLogBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText("资金记录");
    }

    @Override
    protected void initView() {
        // 配置WebView
        wkVBinding.webView.getSettings().setJavaScriptEnabled(true);
        wkVBinding.webView.getSettings().setDomStorageEnabled(true);
        wkVBinding.webView.addJavascriptInterface(new WebAppInterface(), "Android");
        wkVBinding.webView.setWebViewClient(new WebViewClient());
        
        loadAmountLog();
    }

    private void loadAmountLog() {
        String uid = WKConfig.getInstance().getUid();
        Map<String, String> params = new HashMap<>();
        params.put("uid", uid);
        params.put("page_size", "20");
        params.put("page_index", "1");

        HttpRequest.getInstance().get("http://152.42.170.13:8787/V2/getAmountLog", 
            params, new UserService.IHttpRequestCallback() {
                @Override
                public void onSuccess(String data) {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        runOnUiThread(() -> {
                            String htmlContent = "<!DOCTYPE html>" +
                                    "<html>" +
                                    "<head>" +
                                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                                    "<style>" +
                                    "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                                    "body { font-family: Arial, sans-serif; background: #f5f5f5; padding: 20px; }" +
                                    ".record-item { background: white; border-radius: 10px; padding: 15px; margin-bottom: 15px; box-shadow: 0 2px 6px rgba(0,0,0,0.1); }" +
                                    ".record-header { display: flex; justify-content: space-between; margin-bottom: 10px; }" +
                                    ".record-type { color: #666; font-size: 14px; }" +
                                    ".record-amount { font-size: 16px; font-weight: bold; }" +
                                    ".amount-plus { color: #4CAF50; }" +
                                    ".amount-minus { color: #F44336; }" +
                                    ".record-info { color: #999; font-size: 12px; }" +
                                    ".record-status { margin-top: 5px; font-size: 12px; }" +
                                    ".status-pending { color: #FFC107; }" +
                                    ".status-rejected { color: #F44336; }" +
                                    ".status-approved { color: #4CAF50; }" +
                                    ".loading { text-align: center; padding: 20px; color: #666; }" +
                                    ".no-more { text-align: center; padding: 20px; color: #999; }" +
                                    "</style>" +
                                    "</head>" +
                                    "<body>" +
                                    "<div id='recordList'></div>" +
                                    "<div id='loading' class='loading' style='display: none;'>加载中...</div>" +
                                    "<div id='noMore' class='no-more' style='display: none;'>没有更多记录了</div>" +
                                    "<script>" +
                                    "let pageIndex = 1;" +
                                    "let isLoading = false;" +
                                    "let hasMore = true;" +
                                    "let records = " + data + ";" +
                                    
                                    "function getTypeText(type) {" +
                                    "  switch(type) {" +
                                    "    case 1: return '充值';" +
                                    "    case 2: return '充值';" +
                                    "    case 3: return '提现';" +
                                    "    case 4: return '提现';" +
                                    "    default: return '未知';" +
                                    "  }" +
                                    "}" +
                                    
                                    "function getStatusText(status) {" +
                                    "  switch(status) {" +
                                    "    case 1: return '<span class=\"status-pending\">待审批</span>';" +
                                    "    case 2: return '<span class=\"status-rejected\">已驳回</span>';" +
                                    "    case 3: return '<span class=\"status-approved\">已通过</span>';" +
                                    "    default: return '未知状态';" +
                                    "  }" +
                                    "}" +
                                    
                                    "function formatTime(timestamp) {" +
                                    "  const date = new Date(timestamp * 1000);" +
                                    "  return date.getFullYear() + '-' + " +
                                    "    String(date.getMonth() + 1).padStart(2, '0') + '-' + " +
                                    "    String(date.getDate()).padStart(2, '0') + ' ' + " +
                                    "    String(date.getHours()).padStart(2, '0') + ':' + " +
                                    "    String(date.getMinutes()).padStart(2, '0');" +
                                    "}" +
                                    
                                    "function renderRecords() {" +
                                    "  const container = document.getElementById('recordList');" +
                                    "  records.list.forEach(record => {" +
                                    "    const isNegative = parseFloat(record.amount) < 0;" +
                                    "    container.innerHTML += `" +
                                    "      <div class='record-item'>" +
                                    "        <div class='record-header'>" +
                                    "          <span class='record-type'>${getTypeText(record.type)}</span>" +
                                    "          <span class='record-amount ${isNegative ? 'amount-minus' : 'amount-plus'}'>" +
                                    "            ${isNegative ? '' : '+'}${record.amount}" +
                                    "          </span>" +
                                    "        </div>" +
                                    "        <div class='record-info'>" +
                                    "          <div>订单号：${record.order_sn}</div>" +
                                    "          <div>时间：${formatTime(record.add_time)}</div>" +
                                    "          ${record.bank_info ? `<div>银行卡：${record.bank_info.bank_name} (${record.bank_info.bank_number})</div>` : ''}" +
                                    "        </div>" +
                                    "        <div class='record-status'>" +
                                    "          ${getStatusText(record.status)}" +
                                    "        </div>" +
                                    "      </div>" +
                                    "    `;" +
                                    "  });" +
                                    "  hasMore = records.list.length >= 20;" +
                                    "  document.getElementById('noMore').style.display = !hasMore ? 'block' : 'none';" +
                                    "}" +
                                    
                                    "function loadMore() {" +
                                    "  if (isLoading || !hasMore) return;" +
                                    "  isLoading = true;" +
                                    "  document.getElementById('loading').style.display = 'block';" +
                                    "  pageIndex++;" +
                                    "  Android.loadMoreRecords(pageIndex);" +
                                    "}" +
                                    
                                    "window.onscroll = function() {" +
                                    "  if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 100) {" +
                                    "    loadMore();" +
                                    "  }" +
                                    "};" +
                                    
                                    "renderRecords();" +
                                    "</script>" +
                                    "</body>" +
                                    "</html>";

                            wkVBinding.webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(int code, String msg) {
                    runOnUiThread(() -> {
                        Toast.makeText(AmountLogActivity.this, msg, Toast.LENGTH_SHORT).show();
                    });
                }
            });
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void loadMoreRecords(int pageIndex) {
            String uid = WKConfig.getInstance().getUid();
            Map<String, String> params = new HashMap<>();
            params.put("uid", uid);
            params.put("page_size", "20");
            params.put("page_index", String.valueOf(pageIndex));

            HttpRequest.getInstance().get("http://152.42.170.13:8787/V2/getAmountLog", 
                params, new UserService.IHttpRequestCallback() {
                    @Override
                    public void onSuccess(String data) {
                        runOnUiThread(() -> {
                            wkVBinding.webView.evaluateJavascript(
                                "(() => {" +
                                "  isLoading = false;" +
                                "  document.getElementById('loading').style.display = 'none';" +
                                "  const newData = " + data + ";" +
                                "  records.list = records.list.concat(newData.list);" +
                                "  renderRecords();" +
                                "})()",
                                null
                            );
                        });
                    }

                    @Override
                    public void onError(int code, String msg) {
                        runOnUiThread(() -> {
                            Toast.makeText(AmountLogActivity.this, msg, Toast.LENGTH_SHORT).show();
                            wkVBinding.webView.evaluateJavascript(
                                "(() => {" +
                                "  isLoading = false;" +
                                "  document.getElementById('loading').style.display = 'none';" +
                                "})()",
                                null
                            );
                        });
                    }
                });
        }
    }
} 