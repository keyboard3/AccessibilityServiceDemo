package com.keyboard3.accessibilityservicedemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.keyboard3.accessibilityservicedemo.utils.PrefrenceUtil;
import com.keyboard3.accessibilityservicedemo.window.FloatingBallView;
import com.keyboard3.accessibilityservicedemo.window.FloatingWindowManager;

public class MainActivity extends AppCompatActivity {
    int REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.init(getApplication());
        if (checkOverlayPermission()) {
            showWindow();
            finish();
        }
        //setContentView(R.layout.activity_main);
    }

    private void showWindow() {
        FloatingWindowManager fwm = new FloatingWindowManager(MainActivity.this);
        fwm.addView(new FloatingBallView(MainActivity.this, fwm));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && resultCode == RESULT_OK) {
            showWindow();
            finish();
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

    public static class OpenEvent {
        public OpenEvent(String key, int type) {
            this.key = key;
            this.type = type;
        }

        @Override
        public String toString() {
            return "OpenEvent{" +
                    "value='" + value + '\'' +
                    ", key='" + key + '\'' +
                    ", type=" + type +
                    '}';
        }

        public String value;
        public String key;
        public int type;
    }
}
