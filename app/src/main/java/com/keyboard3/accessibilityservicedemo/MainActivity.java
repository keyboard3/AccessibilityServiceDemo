package com.keyboard3.accessibilityservicedemo;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.opengl.ETC1;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    int REQUEST_CODE = 101;
    private EditText etProxy;
    private PrefrenceUtil prefrenceUtil;
    public static final String PROXY = "proxy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefrenceUtil = PrefrenceUtil.getInstance(this);

        findViewById(R.id.btn_layout).setOnClickListener(this);
        findViewById(R.id.btn_overdraw).setOnClickListener(this);
        findViewById(R.id.btn_top).setOnClickListener(this);
        findViewById(R.id.btn_gpu_mode).setOnClickListener(this);
        findViewById(R.id.btn_proxy).setOnClickListener(this);
        etProxy = findViewById(R.id.et_proxy);

        etProxy.setText(prefrenceUtil.getString(PROXY, "192.168.0.200"));
    }

    private void showWindow() {
        if (AccessibilityUtil.checkAccessibility(MainActivity.this)) {
            FloatingWindowManager floatingWindowManager = new FloatingWindowManager(MainActivity.this);
            floatingWindowManager.addView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && resultCode == RESULT_OK) {
            showWindow();
        }
    }

    private boolean checkOverlayPermission() {
        boolean success = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            success = Settings.canDrawOverlays(this);
            if (!success) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), REQUEST_CODE);
                Toast.makeText(this, "请授予悬浮框权限", Toast.LENGTH_SHORT).show();
            }
        }
        return success;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_layout:
                if (AccessibilityUtil.checkAccessibility(MainActivity.this)) {
                    String key = "layout bounds";//Show layout bounds
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        key = "布局边界";
                    }
                    EventBus.getDefault().post(new OpenEvent(key, 1));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings"));
                    startActivity(intent);
                }
                break;
            case R.id.btn_gpu_mode:
                if (AccessibilityUtil.checkAccessibility(MainActivity.this)) {
                    String key = "Profile GPU rendering";//Show layout bounds
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        String model = android.os.Build.MODEL;
                        if (model.startsWith("SM")) {
                            key = "GPU 显示";
                        } else {
                            key = "GPU 呈现";
                        }
                    }
                    EventBus.getDefault().post(new OpenEvent(key, 3));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings"));
                    startActivity(intent);
                }
                break;
            case R.id.btn_overdraw:
                if (AccessibilityUtil.checkAccessibility(MainActivity.this)) {
                    String key = "overdraw";//Debug GPU overdraw
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        key = "过度";
                    }
                    EventBus.getDefault().post(new OpenEvent(key, 2));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings"));
                    startActivity(intent);
                }
                break;
            case R.id.btn_proxy:
                if (AccessibilityUtil.checkAccessibility(MainActivity.this)) {
                    String key = "connected";
                    if ("zh".equals(Locale.getDefault().getLanguage())) {
                        key = "已连接";
                    }
                    OpenEvent event = new OpenEvent(key, 4);
                    event.value = etProxy.getText().toString();
                    if (!TextUtils.isEmpty(event.value)) {
                        prefrenceUtil.setString(PROXY, event.value);
                    }
                    EventBus.getDefault().post(event);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setAction("android.settings.WIFI_IP_SETTINGS");
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity"));
                    startActivity(intent);
                }
                break;
            case R.id.btn_top:
                if (checkOverlayPermission()) {
                    showWindow();
                }
            default:
        }
    }

    public static class OpenEvent {
        public OpenEvent(String key, int type) {
            this.key = key;
            this.type = type;
        }

        public String value;
        public String key;
        public int type;
    }
}
