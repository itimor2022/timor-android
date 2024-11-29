package com.chat.uikit.money;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.TextUtils;
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
import com.chat.base.utils.WKToastUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActMoneyManagementBinding;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MoneyManagementActivity extends WKBaseActivity<ActMoneyManagementBinding> {

    @Override
    protected ActMoneyManagementBinding getViewBinding() {
        return ActMoneyManagementBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.money_management);
    }

    @Override
    protected void initView() {
        // 配置WebView
        wkVBinding.webView.getSettings().setJavaScriptEnabled(true);
        wkVBinding.webView.getSettings().setDomStorageEnabled(true);
        wkVBinding.webView.addJavascriptInterface(new WebAppInterface(), "Android");
        wkVBinding.webView.setWebViewClient(new WebViewClient());
        
        // 加载用户余额
        loadUserAmount();
    }

    public class WebAppInterface {
        @JavascriptInterface
        public String getUid() {
            return WKConfig.getInstance().getUid();
        }

        @JavascriptInterface
        public void showConfirmDialog(String title, String message) {
            runOnUiThread(() -> {
                new AlertDialog.Builder(MoneyManagementActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("确认", (dialog, which) -> {
                        wkVBinding.webView.evaluateJavascript(
                            "(() => { " +
                            "  const amount = document.getElementById('amount').value;" +
                            "  const bankId = document.getElementById('bankSelect').value;" +
                            "  return JSON.stringify({amount: amount, bankId: bankId});" +
                            "})()",
                            value -> {
                                try {
                                    WKLogUtils.e("提现请求", "JavaScript返回数据: " + value);
                                    if (value != null && !value.equals("null")) {
                                        // 处理转义字符
                                        String jsonStr = value.replace("\\\"", "\"")  // 替换 \" 为 "
                                            .replaceAll("^\"|\"$", "");  // 移除首尾的引号
                                        WKLogUtils.e("提现请求", "处理后的JSON字符串: " + jsonStr);
                                        
                                        // 解析 JSON
                                        JSONObject data = new JSONObject(jsonStr);
                                        String amount = data.getString("amount");
                                        String bankId = data.getString("bankId");
                                        
                                        WKLogUtils.e("提现请求", "解析结果 - amount: " + amount + ", bankId: " + bankId);
                                        
                                        if (TextUtils.isEmpty(amount) || TextUtils.isEmpty(bankId)) {
                                            showSimpleToast("请填写完整信息");
                                            return;
                                        }
                                        
                                        // 验证金额格式
                                        try {
                                            float amountValue = Float.parseFloat(amount);
                                            if (amountValue <= 0) {
                                                showSimpleToast("提现金额必须大于0");
                                                return;
                                            }
                                        } catch (NumberFormatException e) {
                                            WKLogUtils.e("提现请求", "金额格式错误: " + e.getMessage());
                                            showSimpleToast("请输入有效的金额");
                                            return;
                                        }
                                        
                                        // 提交提现请求
                                        submitWithdraw(amount, bankId);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    WKLogUtils.e("提现请求", "数据解析异常: " + e.getMessage());
                                    WKLogUtils.e("提现请求", "异常堆栈: " + android.util.Log.getStackTraceString(e));
                                    showSimpleToast("提交数据有误，请重试");
                                }
                            }
                        );
                    })
                    .setNegativeButton("取消", null)
                    .show();
            });
        }

        @JavascriptInterface
        public void gotoBankManagement() {
            runOnUiThread(() -> {
                startActivity(new Intent(MoneyManagementActivity.this, BankManagementActivity.class));
            });
        }

        @JavascriptInterface
        public void gotoAmountLog() {
            runOnUiThread(() -> {
                WKLogUtils.e("资金记录", "点击资金记录按钮");
                Intent intent = new Intent(MoneyManagementActivity.this, AmountLogActivity.class);
                startActivity(intent);
            });
        }
    }

    private void submitWithdraw(String amount, String bankId) {
        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("uid", WKConfig.getInstance().getUid());
        params.put("amount", amount);
        params.put("bank_id", bankId);

        // 打印请求参数
        WKLogUtils.e("提现请求", "开始提现请求");
        WKLogUtils.e("提现请求", "请求URL: http://152.42.170.13:8787/V2/withdraw");
        WKLogUtils.e("提现请求", "请求参数: uid=" + params.get("uid") 
            + ", amount=" + params.get("amount") 
            + ", bank_id=" + params.get("bank_id"));

        // 调用提现接口
        HttpRequest.getInstance().get("http://152.42.170.13:8787/V2/withdraw", 
            params, new UserService.IHttpRequestCallback() {
                @Override
                public void onSuccess(String data) {
                    WKLogUtils.e("提现请求", "收到响应数据: " + data);
                    
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            boolean success = jsonObject.optBoolean("success", false);
                            String message = jsonObject.optString("message", "提现失败，请重试");
                            
                            WKLogUtils.e("提现请求", "解析结果 - success: " + success + ", message: " + message);
                            
                            if (success) {
                                // 提现成功
                                WKLogUtils.e("提现请求", "提现成功");
                                showSimpleToast(message);
                                // 刷新余额
                                loadUserAmount();
                                // 清空表单
                                wkVBinding.webView.evaluateJavascript(
                                    "(() => { " +
                                    "  document.getElementById('amount').value = '';" +
                                    "  document.getElementById('bankSelect').value = '';" +
                                    "  document.getElementById('amountError').textContent = '';" +
                                    "  document.getElementById('bankError').textContent = '';" +
                                    "})()",
                                    null
                                );
                            } else {
                                // 提现失败，显示具体错误信息
                                WKLogUtils.e("提现请求", "提现失败: " + message);
                                showSimpleToast(message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            WKLogUtils.e("提现请求", "解析异常: " + e.getMessage());
                            WKLogUtils.e("提现请求", "异常堆栈: " + android.util.Log.getStackTraceString(e));
                            showSimpleToast("提现失败，请重试 [" + e.getMessage() + "]");
                        }
                    });
                }

                @Override
                public void onError(int code, String msg) {
                    // 打印错误信息
                    WKLogUtils.e("提现请求", "网络错误");
                    WKLogUtils.e("提现请求", "错误码: " + code);
                    WKLogUtils.e("提现请求", "错误信息: " + msg);
                    
                    runOnUiThread(() -> {
                        String errorMsg = TextUtils.isEmpty(msg) ? "网络请求失败，请重试" : msg;
                        showSimpleToast(errorMsg);
                    });
                }
            });
    }

    private void loadUserAmount() {
        String uid = WKConfig.getInstance().getUid();
        UserService.getInstance().getUserAmount(uid, new UserService.IHttpRequestCallback() {
            @Override
            public void onSuccess(String data) {
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONObject list = jsonObject.optJSONObject("list");
                    if (list != null) {
                        String amount = list.optString("amount");
                        runOnUiThread(() -> {
                            String htmlContent = "<!DOCTYPE html>" +
                                    "<html>" +
                                    "<head>" +
                                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                                    "<style>" +
                                    "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                                    "body { font-family: Arial, sans-serif; background: #f5f5f5; padding: 20px; }" +
                                    ".card { background: white; border-radius: 15px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 6px rgba(0,0,0,0.1); }" +
                                    ".balance-title { color: #666; font-size: 16px; margin-bottom: 10px; }" +
                                    ".balance-amount { color: #2196F3; font-size: 32px; font-weight: bold; margin-bottom: 20px; }" +
                                    ".input-field { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px; margin-bottom: 15px; font-size: 16px; }" +
                                    ".select-field { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px; margin-bottom: 15px; font-size: 16px; background: white; }" +
                                    ".button { background: #2196F3; color: white; padding: 15px; border-radius: 10px; text-align: center; margin: 10px 0; font-size: 16px; border: none; width: 100%; cursor: pointer; }" +
                                    ".button:disabled { background: #ccc; }" +
                                    ".error-text { color: #f44336; font-size: 14px; margin-top: -10px; margin-bottom: 10px; }" +
                                    ".menu-item { background: white; padding: 15px; border-radius: 10px; margin: 10px 0; display: flex; align-items: center; justify-content: space-between; }" +
                                    ".menu-item-text { color: #333; font-size: 16px; }" +
                                    ".menu-item-arrow { color: #999; }" +
                                    "</style>" +
                                    "</head>" +
                                    "<body>" +
                                    "<div class='card'>" +
                                    "<div class='balance-title'>当前余额</div>" +
                                    "<div class='balance-amount'>¥" + amount + "</div>" +
                                    "<input type='number' id='amount' class='input-field' placeholder='请输入提现金额' />" +
                                    "<div id='amountError' class='error-text'></div>" +
                                    "<select id='bankSelect' class='select-field'>" +
                                    "<option value=''>请选择收款账户</option>" +
                                    "</select>" +
                                    "<div id='bankError' class='error-text'></div>" +
                                    "<button onclick='submitWithdraw()' class='button'>确认提现</button>" +
                                    "</div>" +
                                    "<div class='menu-item' onclick='manageBankCards()'>" +
                                    "<span class='menu-item-text'>银行卡管理</span>" +
                                    "<span class='menu-item-arrow'>›</span>" +
                                    "</div>" +
                                    "<div class='menu-item' onclick='gotoAmountLog()'>" +
                                    "<span class='menu-item-text'>资金记录</span>" +
                                    "<span class='menu-item-arrow'>›</span>" +
                                    "</div>" +
                                    "<script>" +
                                    "let bankList = [];" +
                                    "let maxAmount = " + amount + ";" +
                                    "function loadBankCards() {" +
                                    "  fetch('http://152.42.170.13:8787/V2/getBank?uid=' + Android.getUid())" +
                                    "  .then(response => response.json())" +
                                    "  .then(data => {" +
                                    "    bankList = data.list;" +
                                    "    const select = document.getElementById('bankSelect');" +
                                    "    select.innerHTML = '<option value=\"\">请选择收款账户</option>';" +
                                    "    data.list.forEach(bank => {" +
                                    "      select.innerHTML += `<option value=\"${bank.id}\">${bank.bank_name} (${formatBankNumber(bank.bank_number)})</option>`;" +
                                    "    });" +
                                    "  });" +
                                    "}" +
                                    "function formatBankNumber(number) {" +
                                    "  return '**** **** **** ' + number.slice(-4);" +
                                    "}" +
                                    "function validateForm() {" +
                                    "  const amount = document.getElementById('amount').value;" +
                                    "  const bankId = document.getElementById('bankSelect').value;" +
                                    "  let isValid = true;" +
                                    "  if (!amount) {" +
                                    "    document.getElementById('amountError').textContent = '请输入提现金额';" +
                                    "    isValid = false;" +
                                    "  } else if (amount <= 0) {" +
                                    "    document.getElementById('amountError').textContent = '提现金额必须大于0';" +
                                    "    isValid = false;" +
                                    "  } else if (amount > maxAmount) {" +
                                    "    document.getElementById('amountError').textContent = '提现金额不能大于余额';" +
                                    "    isValid = false;" +
                                    "  } else {" +
                                    "    document.getElementById('amountError').textContent = '';" +
                                    "  }" +
                                    "  if (!bankId) {" +
                                    "    document.getElementById('bankError').textContent = '请选择收款账户';" +
                                    "    isValid = false;" +
                                    "  } else {" +
                                    "    document.getElementById('bankError').textContent = '';" +
                                    "  }" +
                                    "  return isValid;" +
                                    "}" +
                                    "function submitWithdraw() {" +
                                    "  if (!validateForm()) return;" +
                                    "  const amount = document.getElementById('amount').value;" +
                                    "  const bankId = document.getElementById('bankSelect').value;" +
                                    "  const selectedBank = bankList.find(bank => bank.id == bankId);" +
                                    "  Android.showConfirmDialog('确认提现', " +
                                    "    `确认提现 ¥${amount} 到账户：${selectedBank.bank_name} (${formatBankNumber(selectedBank.bank_number)})?`);" +
                                    "}" +
                                    "function manageBankCards() {" +
                                    "  Android.gotoBankManagement();" +
                                    "}" +
                                    "function gotoAmountLog() {" +
                                    "  Android.gotoAmountLog();" +
                                    "}" +
                                    "loadBankCards();" +
                                    "</script>" +
                                    "</body>" +
                                    "</html>";

                            wkVBinding.webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(int code, String msg) {
                runOnUiThread(() -> {
                    String errorHtml = "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "<style>" +
                            "body { font-family: Arial, sans-serif; text-align: center; padding: 20px; }" +
                            ".error-message { color: #666; font-size: 16px; margin-top: 20px; }" +
                            "</style>" +
                            "</head>" +
                            "<body>" +
                            "<div class='error-message'>" + msg + "</div>" +
                            "</body>" +
                            "</html>";
                    wkVBinding.webView.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null);
                });
            }
        });
    }

    private void showSimpleToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }
} 