package com.chat.uikit.money;

import android.text.TextUtils;
import android.widget.TextView;

import com.chat.base.base.WKBaseActivity;
import com.chat.base.config.WKConfig;
import com.chat.base.net.HttpRequest;
import com.chat.base.net.UserService;
import com.chat.base.utils.WKToastUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActAddBankCardBinding;

import java.util.HashMap;
import java.util.Map;

public class AddBankCardActivity extends WKBaseActivity<ActAddBankCardBinding> {

    @Override
    protected ActAddBankCardBinding getViewBinding() {
        return ActAddBankCardBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.add_bank_card);
    }

    @Override
    protected void initView() {
        wkVBinding.submitBtn.setOnClickListener(v -> {
            String bankName = wkVBinding.bankNameEt.getText().toString().trim();
            String bankNumber = wkVBinding.bankNumberEt.getText().toString().trim();
            String bankHome = wkVBinding.bankHomeEt.getText().toString().trim();
            String nikeName = wkVBinding.nikeNameEt.getText().toString().trim();

            if (TextUtils.isEmpty(bankName)) {
                WKToastUtils.getInstance().showToast("请输入银行名称");
                return;
            }
            if (TextUtils.isEmpty(bankNumber)) {
                WKToastUtils.getInstance().showToast("请输入银行卡号");
                return;
            }
            if (TextUtils.isEmpty(bankHome)) {
                WKToastUtils.getInstance().showToast("请输入开户行");
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("bank_home", bankHome);
            params.put("bank_name", bankName);
            params.put("bank_number", bankNumber);
            params.put("nike_name", nikeName);
            params.put("uid", WKConfig.getInstance().getUid());

            HttpRequest.getInstance().get("http://152.42.170.13:8787/V2/bindBankCard", 
                params, new UserService.IHttpRequestCallback() {
                    @Override
                    public void onSuccess(String data) {
                        runOnUiThread(() -> {
                            WKToastUtils.getInstance().showToast("添加成功");
                            setResult(RESULT_OK);
                            finish();
                        });
                    }

                    @Override
                    public void onError(int code, String msg) {
                        runOnUiThread(() -> {
                            WKToastUtils.getInstance().showToast(msg);
                        });
                    }
                });
        });
    }
} 