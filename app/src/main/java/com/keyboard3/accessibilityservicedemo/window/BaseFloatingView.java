package com.keyboard3.accessibilityservicedemo.window;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.keyboard3.accessibilityservicedemo.R;

/**
 * Created by keyboard3 on 2018/1/25.
 */

public abstract class BaseFloatingView extends LinearLayout {
    protected final FloatingWindowManager mFwm;
    protected final Context mContext;
    private int downX;
    private int downY;

    public BaseFloatingView(Context context, FloatingWindowManager windowManager) {
        super(context);
        mContext = context;
        mFwm = windowManager;
    }

    protected abstract void initView();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getRawX();
                downY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                FloatingWindowManager.layout_params.x += x - downX;
                FloatingWindowManager.layout_params.y += y - downY;
                mFwm.mWm.updateViewLayout(this, FloatingWindowManager.layout_params);
                downX = x;
                downY = y;
                break;
            default:
        }
        return super.onTouchEvent(event);
    }
}
