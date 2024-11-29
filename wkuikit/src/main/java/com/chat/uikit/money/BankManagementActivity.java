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
import com.chat.base.utils.WKLogUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActBankManagementBinding;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BankManagementActivity extends WKBaseActivity<ActBankManagementBinding> {

    @Override
    protected ActBankManagementBinding getViewBinding() {
        return ActBankManagementBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.bank_management);
    }

    @Override
    protected void initView() {
        try {
            // 配置WebView
            wkVBinding.webView.getSettings().setJavaScriptEnabled(true);
            wkVBinding.webView.getSettings().setDomStorageEnabled(true);
            wkVBinding.webView.addJavascriptInterface(new WebAppInterface(), "Android");
            wkVBinding.webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    // 加载错误时显示错误页面
                    showErrorPage();
                }
            });
            
            loadBankCards();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorPage();
        }
    }

    private void showErrorPage() {
        runOnUiThread(() -> {
            String errorHtml = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; text-align: center; padding: 20px; }" +
                    ".error-message { color: #666; font-size: 16px; margin-top: 20px; }" +
                    ".retry-button { background: #2196F3; color: white; padding: 10px 20px; " +
                    "border: none; border-radius: 5px; margin-top: 20px; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='error-message'>加载失败，请重试</div>" +
                    "<button class='retry-button' onclick='retry()'>重新加载</button>" +
                    "<script>" +
                    "function retry() { window.location.reload(); }" +
                    "</script>" +
                    "</body>" +
                    "</html>";
            wkVBinding.webView.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null);
        });
    }

    private void loadBankCards() {
        String uid = WKConfig.getInstance().getUid();
        Map<String, String> params = new HashMap<>();
        params.put("uid", uid);

        HttpRequest.getInstance().get("http://152.42.170.13:8787/V2/getBank", params, 
            new UserService.IHttpRequestCallback() {
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
                                    ".tab-container { display: flex; background: white; margin: -20px -20px 20px -20px; }" +
                                    ".tab { flex: 1; text-align: center; padding: 15px; color: #666; position: relative; }" +
                                    ".tab.active { color: #2196F3; }" +
                                    ".tab.active:after { content: ''; position: absolute; bottom: 0; left: 25%; width: 50%; height: 2px; background: #2196F3; }" +
                                    ".card { background: white; border-radius: 10px; padding: 20px; margin-bottom: 15px; box-shadow: 0 2px 6px rgba(0,0,0,0.1); }" +
                                    ".bank-name { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 12px; }" +
                                    ".bank-info { color: #666; font-size: 14px; }" +
                                    ".bank-item { margin: 8px 0; }" +
                                    ".form-group { margin-bottom: 20px; }" +
                                    ".label { color: #666; font-size: 14px; margin-bottom: 8px; }" +
                                    ".input { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 16px; }" +
                                    ".button { background: #2196F3; color: white; width: 100%; padding: 15px; border: none; border-radius: 8px; font-size: 16px; margin-top: 20px; }" +
                                    ".error-text { color: #f44336; font-size: 14px; margin-top: 4px; }" +
                                    "</style>" +
                                    "</head>" +
                                    "<body>" +
                                    "<div class='tab-container'>" +
                                    "<div class='tab active' onclick='switchTab(0)'>银行卡列表</div>" +
                                    "<div class='tab' onclick='switchTab(1)'>添加银行卡</div>" +
                                    "</div>" +
                                    "<div id='listContent'></div>" +
                                    "<div id='addContent' style='display:none'>" +
                                    "<div class='card'>" +
                                    "<div class='form-group'>" +
                                    "<div class='label'>银行名称</div>" +
                                    "<input type='text' class='input' id='bankName' placeholder='请输入银行名称'>" +
                                    "<div class='error-text' id='bankNameError'></div>" +
                                    "</div>" +
                                    "<div class='form-group'>" +
                                    "<div class='label'>银行卡号</div>" +
                                    "<input type='text' class='input' id='bankNumber' placeholder='请输入银行卡号'>" +
                                    "<div class='error-text' id='bankNumberError'></div>" +
                                    "</div>" +
                                    "<div class='form-group'>" +
                                    "<div class='label'>开户行</div>" +
                                    "<input type='text' class='input' id='bankHome' placeholder='请输入开户行'>" +
                                    "<div class='error-text' id='bankHomeError'></div>" +
                                    "</div>" +
                                    "<div class='form-group'>" +
                                    "<div class='label'>持卡人姓名</div>" +
                                    "<input type='text' class='input' id='nikeName' placeholder='请输入持卡人姓名'>" +
                                    "</div>" +
                                    "<button class='button' onclick='submitForm()'>添加银行卡</button>" +
                                    "</div>" +
                                    "</div>" +
                                    "<script>" +
                                    "let bankList = " + data + ";" +
                                    "function formatBankNumber(number) {" +
                                    "  return '**** **** **** ' + number.slice(-4);" +
                                    "}" +
                                    "function renderList() {" +
                                    "  const container = document.getElementById('listContent');" +
                                    "  container.innerHTML = '';" +
                                    "  bankList.list.forEach(bank => {" +
                                    "    container.innerHTML += `" +
                                    "      <div class='card'>" +
                                    "        <div class='bank-name'>${bank.bank_name}</div>" +
                                    "        <div class='bank-info'>" +
                                    "          <div class='bank-item'>开户行：${bank.bank_home}</div>" +
                                    "          <div class='bank-item'>卡号：${formatBankNumber(bank.bank_number)}</div>" +
                                    "          <div class='bank-item'>持卡人：${bank.nike_name}</div>" +
                                    "        </div>" +
                                    "      </div>" +
                                    "    `;" +
                                    "  });" +
                                    "}" +
                                    "function switchTab(index) {" +
                                    "  const tabs = document.querySelectorAll('.tab');" +
                                    "  tabs.forEach(tab => tab.classList.remove('active'));" +
                                    "  tabs[index].classList.add('active');" +
                                    "  document.getElementById('listContent').style.display = index === 0 ? 'block' : 'none';" +
                                    "  document.getElementById('addContent').style.display = index === 1 ? 'block' : 'none';" +
                                    "}" +
                                    "function validateForm() {" +
                                    "  let isValid = true;" +
                                    "  const bankName = document.getElementById('bankName').value;" +
                                    "  const bankNumber = document.getElementById('bankNumber').value;" +
                                    "  const bankHome = document.getElementById('bankHome').value;" +
                                    "  if (!bankName) {" +
                                    "    document.getElementById('bankNameError').textContent = '请输入银行名称';" +
                                    "    isValid = false;" +
                                    "  } else {" +
                                    "    document.getElementById('bankNameError').textContent = '';" +
                                    "  }" +
                                    "  if (!bankNumber) {" +
                                    "    document.getElementById('bankNumberError').textContent = '请输入银行卡号';" +
                                    "    isValid = false;" +
                                    "  } else {" +
                                    "    document.getElementById('bankNumberError').textContent = '';" +
                                    "  }" +
                                    "  if (!bankHome) {" +
                                    "    document.getElementById('bankHomeError').textContent = '请输入开户行';" +
                                    "    isValid = false;" +
                                    "  } else {" +
                                    "    document.getElementById('bankHomeError').textContent = '';" +
                                    "  }" +
                                    "  return isValid;" +
                                    "}" +
                                    "function submitForm() {" +
                                    "  if (!validateForm()) return;" +
                                    "  const formData = {" +
                                    "    bankName: document.getElementById('bankName').value," +
                                    "    bankNumber: document.getElementById('bankNumber').value," +
                                    "    bankHome: document.getElementById('bankHome').value," +
                                    "    nikeName: document.getElementById('nikeName').value" +
                                    "  };" +
                                    "  Android.addBankCard(JSON.stringify(formData));" +
                                    "}" +
                                    "renderList();" +
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
                        Toast.makeText(BankManagementActivity.this, msg, Toast.LENGTH_SHORT).show();
                    });
                }
            });
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void addBankCard(String formData) {
            try {
                JSONObject data = new JSONObject(formData);
                Map<String, String> params = new HashMap<>();
                params.put("uid", WKConfig.getInstance().getUid());
                params.put("bank_name", data.getString("bankName"));
                params.put("bank_number", data.getString("bankNumber"));
                params.put("bank_home", data.getString("bankHome"));
                params.put("nike_name", data.getString("nikeName"));

                WKLogUtils.e("添加银行卡", "请求参数: " + params.toString());

                HttpRequest.getInstance().get("http://152.42.170.13:8787/V2/bindBankCard", 
                    params, new UserService.IHttpRequestCallback() {
                        @Override
                        public void onSuccess(String response) {
                            WKLogUtils.e("添加银行卡", "响应数据: " + response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String msg = jsonObject.optString("message", "添加成功");
                                runOnUiThread(() -> {
                                    Toast.makeText(BankManagementActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    // 添加成功后直接刷新整个页面，这样会自动显示列表页
                                    loadBankCards();
                                    // 切换到列表页
                                    wkVBinding.webView.evaluateJavascript(
                                        "(() => { " +
                                        "  switchTab(0);" + // 切换到列表页
                                        "})()",
                                        null
                                    );
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(() -> {
                                    Toast.makeText(BankManagementActivity.this, "添加失败，请重试", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onError(int code, String msg) {
                            WKLogUtils.e("添加银行卡", "错误: code=" + code + ", msg=" + msg);
                            runOnUiThread(() -> {
                                Toast.makeText(BankManagementActivity.this, msg, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
            } catch (Exception e) {
                e.printStackTrace();
                WKLogUtils.e("添加银行卡", "异常: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(BankManagementActivity.this, "添加失败，请重试", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
} 