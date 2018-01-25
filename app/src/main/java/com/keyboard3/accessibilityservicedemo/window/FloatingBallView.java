package com.keyboard3.accessibilityservicedemo.window;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.keyboard3.accessibilityservicedemo.ProxyEditActivity;
import com.keyboard3.accessibilityservicedemo.R;

/**
 * Created by keyboard3 on 2018/1/25.
 */

public class FloatingBallView extends BaseFloatingView {
    public FloatingBallView(Context context, FloatingWindowManager windowManager) {
        super(context, windowManager);
        initView();
    }

    @Override
    protected void initView() {
        inflate(mContext, R.layout.layout_floating_ball, this);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProxyEditActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }
}
