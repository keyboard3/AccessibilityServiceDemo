package com.keyboard3.accessibilityservicedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import com.keyboard3.accessibilityservicedemo.utils.PrefrenceUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

public class ProxyEditActivity extends Activity {
    public static final String PROXY = "proxy";
    private EditText etProxy;
    private PrefrenceUtil prefrenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        etProxy = new EditText(this);

        prefrenceUtil = PrefrenceUtil.getInstance(this);
        etProxy.setText(prefrenceUtil.getString(PROXY, "192.168.0.200"));

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
}