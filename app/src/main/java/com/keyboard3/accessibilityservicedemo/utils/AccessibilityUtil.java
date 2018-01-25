package com.keyboard3.accessibilityservicedemo.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

/**
 * @author keyboard3 on 2018/1/21
 */

public class AccessibilityUtil {
    public static boolean checkAccessibility(Context context) {
        if (!isAccessibilitySettingOn(context)) {
            context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            Toast.makeText(context, "请先开启辅助服务的功能", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 已经开启的包名中是否包含当前应用包名
     *
     * @param context
     * @return
     */
    private static boolean isAccessibilitySettingOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }
        return false;
    }
}
