package com.keyboard3.accessibilityservicedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.keyboard3.accessibilityservicedemo.utils.AccessibilityUtil;
import com.keyboard3.accessibilityservicedemo.utils.PrefrenceUtil;
import com.keyboard3.accessibilityservicedemo.window.ActivityTopView;
import com.keyboard3.accessibilityservicedemo.window.FloatingWindowManager;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

public class ProxyEditActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String PROXY = "proxy";
    private EditText etProxy;
    private PrefrenceUtil prefrenceUtil;
    private FloatingWindowManager fwm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_floating_content);

        findViewById(R.id.btn_layout).setOnClickListener(this);
        findViewById(R.id.btn_overdraw).setOnClickListener(this);
        findViewById(R.id.btn_top).setOnClickListener(this);
        findViewById(R.id.btn_gpu_mode).setOnClickListener(this);
        findViewById(R.id.btn_proxy).setOnClickListener(this);
        findViewById(R.id.btn_proxy_close).setOnClickListener(this);
        findViewById(R.id.tv_close).setOnClickListener(this);
        fwm = new FloatingWindowManager(this);


        etProxy = new EditText(this);
        prefrenceUtil = PrefrenceUtil.getInstance(this);
        etProxy.setText(prefrenceUtil.getString(PROXY, "192.168.0.200"));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_layout:
                if (AccessibilityUtil.checkAccessibility(this)) {
                    String key = "layout bounds";//Show layout bounds
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        key = "布局边界";
                    }
                    EventBus.getDefault().post(new MainActivity.OpenEvent(key, MyAccessibilityService.TYPE_LAYOUT));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings"));
                    this.startActivity(intent);
                    finish();
                }
                break;
            case R.id.btn_gpu_mode:
                if (AccessibilityUtil.checkAccessibility(this)) {
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
                    this.startActivity(intent);
                    finish();
                }
                break;
            case R.id.btn_overdraw:
                if (AccessibilityUtil.checkAccessibility(this)) {
                    String key = "overdraw";//Debug GPU overdraw
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        key = "过度";
                    }
                    EventBus.getDefault().post(new MainActivity.OpenEvent(key, MyAccessibilityService.TYPE_OVERDRAW));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings"));
                    this.startActivity(intent);
                    finish();
                }
                break;
            case R.id.btn_proxy:
                if (AccessibilityUtil.checkAccessibility(this)) {
                    new AlertDialog.Builder(this)
                            .setTitle("代理设置")
                            .setView(etProxy)
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
                            startActivity(intent);
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
                }
                break;
            case R.id.btn_proxy_close:
                if (AccessibilityUtil.checkAccessibility(this)) {
                    String key = "connected";
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        key = "已连接";
                    }
                    MainActivity.OpenEvent event = new MainActivity.OpenEvent(key, MyAccessibilityService.TYPE_PROXY);
                    event.value = "";
                    EventBus.getDefault().post(event);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setAction("android.settings.WIFI_IP_SETTINGS");
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity"));
                    this.startActivity(intent);
                    finish();
                }
                break;
            case R.id.btn_top:
                showWindow();
                break;
            case R.id.tv_close:
                finish();
            default:
        }
    }

    private void showWindow() {
        if (AccessibilityUtil.checkAccessibility(this)) {
            fwm.addView(new ActivityTopView(this, fwm));
            finish();
        }
    }
}