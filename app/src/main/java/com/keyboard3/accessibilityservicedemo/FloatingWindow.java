package com.keyboard3.accessibilityservicedemo;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author keyboard3 on 2018/1/21
 */

public class FloatingWindow extends LinearLayout {

    private final FloatingWindowManager mFwm;
    private final Context mContext;
    private TextView mTvPckage;
    private TextView mTvClass;
    private TextView mTvClose;
    private int downX;
    private int downY;

    public FloatingWindow(Context context, FloatingWindowManager windowManager) {
        super(context);
        mContext = context;
        mFwm = windowManager;
        initView();
    }

    private void initView() {
        inflate(mContext, R.layout.layout_floating, this);

        mTvPckage = findViewById(R.id.tv_package);
        mTvClass = findViewById(R.id.tv_class);
        mTvClose = findViewById(R.id.tv_close);
        mTvClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFwm.removeView();
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeWindow(ChangeEvent event) {
        mTvPckage.setText(event.packageName);
        mTvClass.setText(event.className.replace(event.packageName, ""));
    }

    public static class ChangeEvent {
        public ChangeEvent(String packageName, String className) {
            this.packageName = packageName;
            this.className = className;
        }

        public String packageName;
        public String className;
    }

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
