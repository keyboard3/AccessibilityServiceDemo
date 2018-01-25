package com.keyboard3.accessibilityservicedemo.window;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keyboard3.accessibilityservicedemo.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author keyboard3 on 2018/1/21
 */

public class ActivityTopView extends BaseFloatingView {

    private TextView mTvPckage;
    private TextView mTvClass;
    private TextView mTvClose;

    public ActivityTopView(Context context, FloatingWindowManager windowManager) {
        super(context, windowManager);
        initView();
    }

    @Override
    protected void initView() {
        inflate(mContext, R.layout.layout_floating_activity_top, this);

        mTvPckage = findViewById(R.id.tv_package);
        mTvClass = findViewById(R.id.tv_class);
        mTvClose = findViewById(R.id.tv_close);
        mTvClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFwm.removeView(ActivityTopView.this);
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
}
