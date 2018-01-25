package com.keyboard3.accessibilityservicedemo.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * @author keyboard3 on 2018/1/21
 */

public class FloatingWindowManager {

    private final Context mContext;
    public final WindowManager mWm;
    public static WindowManager.LayoutParams layout_params;

    public FloatingWindowManager(Context context) {
        mContext = context;
        mWm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    static {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layout_params = layoutParams;
    }

    public void addView(LinearLayout mFloatingView) {
        mFloatingView.setLayoutParams(layout_params);
        mWm.addView(mFloatingView, layout_params);
    }

    public void removeView(View mFloatingView) {
        if (mFloatingView != null) {
            mWm.removeView(mFloatingView);
        }
    }
}
