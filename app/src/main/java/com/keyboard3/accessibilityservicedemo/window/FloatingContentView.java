package com.keyboard3.accessibilityservicedemo.window;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.keyboard3.accessibilityservicedemo.MainActivity;
import com.keyboard3.accessibilityservicedemo.MyAccessibilityService;
import com.keyboard3.accessibilityservicedemo.R;
import com.keyboard3.accessibilityservicedemo.utils.AccessibilityUtil;
import com.keyboard3.accessibilityservicedemo.utils.PrefrenceUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

/**
 * Created by keyboard3 on 2018/1/25.
 */

public class FloatingContentView extends BaseFloatingView implements View.OnClickListener {
    private EditText etProxy;
    private PrefrenceUtil prefrenceUtil;
    public static final String PROXY = "proxy";
    private FloatingWindowManager fwm;

    public FloatingContentView(Context context, FloatingWindowManager windowManager) {
        super(context, windowManager);
        initView();
    }

    @Override
    protected void initView() {
        prefrenceUtil = PrefrenceUtil.getInstance(mContext);

        inflate(mContext, R.layout.layout_floating_content, this);
        findViewById(R.id.btn_layout).setOnClickListener(this);
        findViewById(R.id.btn_overdraw).setOnClickListener(this);
        findViewById(R.id.btn_top).setOnClickListener(this);
        findViewById(R.id.btn_gpu_mode).setOnClickListener(this);
        findViewById(R.id.btn_proxy).setOnClickListener(this);
        findViewById(R.id.btn_proxy_close).setOnClickListener(this);
        findViewById(R.id.tv_close).setOnClickListener(this);
        etProxy = findViewById(R.id.et_proxy);
        etProxy.setText(prefrenceUtil.getString(PROXY, "192.168.0.200"));
        fwm = new FloatingWindowManager(mContext);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_layout:
                if (AccessibilityUtil.checkAccessibility(mContext)) {
                    String key = "layout bounds";//Show layout bounds
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        key = "布局边界";
                    }
                    EventBus.getDefault().post(new MainActivity.OpenEvent(key, MyAccessibilityService.TYPE_LAYOUT));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings"));
                    mContext.startActivity(intent);
                    removeWindow();
                }
                break;
            case R.id.btn_gpu_mode:
                if (AccessibilityUtil.checkAccessibility(mContext)) {
                    String key = "Profile GPU rendering";//Show layout bounds
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        String model = android.os.Build.MODEL;
                        if (model.startsWith("SM")) {
                            key = "GPU 显示";
                        } else {
                            key = "GPU 呈现";
                        }
                    }
                    EventBus.getDefault().post(new MainActivity.OpenEvent(key, MyAccessibilityService.TYPE_GPU));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings"));
                    mContext.startActivity(intent);
                    removeWindow();
                }
                break;
            case R.id.btn_overdraw:
                if (AccessibilityUtil.checkAccessibility(mContext)) {
                    String key = "overdraw";//Debug GPU overdraw
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        key = "过度";
                    }
                    EventBus.getDefault().post(new MainActivity.OpenEvent(key, MyAccessibilityService.TYPE_OVERDRAW));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings"));
                    mContext.startActivity(intent);
                    removeWindow();
                }
                break;
            case R.id.btn_proxy:
                if (AccessibilityUtil.checkAccessibility(mContext)) {
                    String key = "connected";
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        key = "已连接";
                    }
                    MainActivity.OpenEvent event = new MainActivity.OpenEvent(key, MyAccessibilityService.TYPE_PROXY);
                    event.value = etProxy.getText().toString();
                    if (!TextUtils.isEmpty(event.value)) {
                        prefrenceUtil.setString(PROXY, event.value);
                    }
                    EventBus.getDefault().post(event);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setAction("android.settings.WIFI_IP_SETTINGS");
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity"));
                    mContext.startActivity(intent);
                    removeWindow();
                }
                break;
            case R.id.btn_proxy_close:
                if (AccessibilityUtil.checkAccessibility(mContext)) {
                    String key = "connected";
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        key = "已连接";
                    }
                    MainActivity.OpenEvent event = new MainActivity.OpenEvent(key, 4);
                    event.value = "";
                    EventBus.getDefault().post(event);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setAction("android.settings.WIFI_IP_SETTINGS");
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity"));
                    mContext.startActivity(intent);
                    removeWindow();
                }
                break;
            case R.id.btn_top:
                showWindow();
            case R.id.tv_close:
                removeWindow();
            default:
        }
    }

    private void removeWindow() {
        if (fwm == null) return;
        fwm.removeView(this);
    }

    private void showWindow() {
        if (AccessibilityUtil.checkAccessibility(mContext)) {
            fwm.addView(new ActivityTopView(mContext, fwm));
        }
    }
}
