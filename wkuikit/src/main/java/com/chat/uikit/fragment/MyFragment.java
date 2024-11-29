package com.chat.uikit.fragment;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.chat.base.base.WKBaseFragment;
import com.chat.base.common.WKCommonModel;
import com.chat.base.config.WKConfig;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.PersonalInfoMenu;
import com.chat.base.net.UserService;
import com.chat.base.ui.Theme;
import com.chat.base.utils.WKLogUtils;
import com.chat.base.utils.WKToastUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.databinding.FragMyLayoutBinding;
import com.chat.uikit.money.AmountLogActivity;
import com.chat.uikit.money.BankManagementActivity;
import com.chat.uikit.money.MoneyManagementActivity;
import com.chat.uikit.user.MyInfoActivity;
import com.xinbida.wukongim.entity.WKChannelType;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 2019-11-12 14:58
 * 我的
 */
public class MyFragment extends WKBaseFragment<FragMyLayoutBinding> {
    private PersonalItemAdapter adapter;
    private Handler handler;
    private static final int UPDATE_INTERVAL = 3000; // 3秒更新一次
    private Runnable updateRunnable;
    private boolean isUpdateRunning = false;

    @Override
    protected FragMyLayoutBinding getViewBinding() {
        return FragMyLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        wkVBinding.recyclerView.setNestedScrollingEnabled(false);
        adapter = new PersonalItemAdapter(new ArrayList<>());
        initAdapter(wkVBinding.recyclerView, adapter);
        
        // 设置数据item
        List<PersonalInfoMenu> endpoints = EndpointManager.getInstance().invokes(EndpointCategory.personalCenter, null);
        
        // 找到新消息通知的位置
        int noticeIndex = -1;
        for (int i = 0; i < endpoints.size(); i++) {
            if (endpoints.get(i).text.equals(getString(R.string.new_msg_notice))) {
                noticeIndex = i;
                break;
            }
        }
        
        // 在新消息通知后面插入资金管理、银行卡管理和资金记录
        if (noticeIndex != -1) {
            // 添加资金管理
            endpoints.add(noticeIndex + 1, new PersonalInfoMenu(
                R.drawable.ic_money_management,
                getString(R.string.money_management),
                () -> {
                    Intent intent = new Intent(getActivity(), MoneyManagementActivity.class);
                    startActivity(intent);
                }
            ));
            
            // 添加银行卡管理
            endpoints.add(noticeIndex + 2, new PersonalInfoMenu(
                R.drawable.ic_bank_management,  // 使用银行卡管理图标
                getString(R.string.bank_management),
                () -> {
                    Intent intent = new Intent(getActivity(), BankManagementActivity.class);
                    startActivity(intent);
                }
            ));
            
            // 添加资金记录
            endpoints.add(noticeIndex + 3, new PersonalInfoMenu(
                R.drawable.ic_amount_log,  // 使用资金记录图标
                getString(R.string.amount_log),
                () -> {
                    Intent intent = new Intent(getActivity(), AmountLogActivity.class);
                    startActivity(intent);
                }
            ));
        }
        
        adapter.setList(endpoints);
        
        // 加载用户余额
        loadUserAmount();
        
        // 初始化Handler和更新任务
        handler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isUpdateRunning) {
                    loadUserAmount();
                    handler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        };
        
        // 开始自动更新
        startAutoUpdate();
    }

    private void startAutoUpdate() {
        isUpdateRunning = true;
        handler.post(updateRunnable);
    }

    private void stopAutoUpdate() {
        isUpdateRunning = false;
        handler.removeCallbacks(updateRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        wkVBinding.nameTv.setText(WKConfig.getInstance().getUserInfo().name);
        wkVBinding.avatarView.showAvatar(WKConfig.getInstance().getUid(), WKChannelType.PERSONAL);
        
        // 每次页面恢复时刷新余额
        loadUserAmount();
        
        if (null != adapter) {
            try {
                WKCommonModel.getInstance().getAppNewVersion(false, version -> {
                    int index = -1;
                    for (int i = 0; i < adapter.getData().size(); i++) {
                        if (getString(R.string.currency).equals(adapter.getData().get(i).text)) {
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        if (version != null && !TextUtils.isEmpty(version.download_url)) {
                            if (!adapter.getData().get(index).isNewVersionIv) {
                                adapter.getData().get(index).setIsNewVersionIv(true);
                                adapter.notifyItemChanged(index);
                            }
                        } else if (adapter.getData().get(index).isNewVersionIv) {
                            adapter.getData().get(index).setIsNewVersionIv(false);
                            adapter.notifyItemChanged(index);
                        }
                    }
                });
            } catch (Exception e) {
                WKLogUtils.w("检查新版本错误");
            }
        }
        startAutoUpdate(); // 恢复自动更新
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoUpdate(); // 暂停自动更新
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAutoUpdate(); // 确保停止更新
        handler.removeCallbacksAndMessages(null);
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
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                // 添加数字变化动画
                                try {
                                    float oldValue = 0f;
                                    if (wkVBinding.balanceTv.getText().length() > 0) {
                                        oldValue = Float.parseFloat(wkVBinding.balanceTv.getText().toString().replace("¥", "").trim());
                                    }
                                    float newValue = Float.parseFloat(amount);
                                    // 只有当金额发生变化时才显示动画
                                    if (oldValue != newValue) {
                                        ValueAnimator animator = ValueAnimator.ofFloat(oldValue, newValue);
                                        animator.setDuration(1000);
                                        animator.setInterpolator(new DecelerateInterpolator());
                                        animator.addUpdateListener(animation -> {
                                            float value = (float) animation.getAnimatedValue();
                                            wkVBinding.balanceTv.setText(String.format("¥%.2f", value));
                                        });
                                        animator.start();

                                        // 添加缩放动画
                                        wkVBinding.balanceTv.setScaleX(0.8f);
                                        wkVBinding.balanceTv.setScaleY(0.8f);
                                        wkVBinding.balanceTv.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(300)
                                            .setInterpolator(new OvershootInterpolator())
                                            .start();
                                    }
                                } catch (NumberFormatException e) {
                                    // 如果解析失败，直接显示
                                    wkVBinding.balanceTv.setText(String.format("¥%s", amount));
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(int code, String msg) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        WKToastUtils.getInstance().showToast(msg);
                    });
                }
            }
        });
    }

    @Override
    protected void initPresenter() {
        wkVBinding.avatarView.setSize(90);
        wkVBinding.refreshLayout.setEnableOverScrollDrag(true);
        wkVBinding.refreshLayout.setEnableLoadMore(false);
        wkVBinding.refreshLayout.setEnableRefresh(false);
        Theme.setPressedBackground(wkVBinding.qrIv);
    }

    @Override
    protected void initListener() {
        adapter.setOnItemClickListener((adapter1, view, position) -> SingleClickUtil.determineTriggerSingleClick(view, view1 -> {
            PersonalInfoMenu menu = (PersonalInfoMenu) adapter1.getItem(position);
            if (menu != null && menu.iPersonalInfoMenuClick != null) {
                menu.iPersonalInfoMenuClick.onClick();
            }
        }));
        SingleClickUtil.onSingleClick(wkVBinding.avatarView, view -> gotoMyInfo());
        SingleClickUtil.onSingleClick(wkVBinding.qrIv, view -> gotoMyInfo());
    }

    void gotoMyInfo() {
        startActivity(new Intent(getActivity(), MyInfoActivity.class));
    }
}
