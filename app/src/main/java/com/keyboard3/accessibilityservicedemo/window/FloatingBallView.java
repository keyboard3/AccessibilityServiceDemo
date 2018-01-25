package com.keyboard3.accessibilityservicedemo.window;

import android.content.Context;
import android.view.View;

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
                FloatingWindowManager fwm = new FloatingWindowManager(mContext);
                fwm.addView(new FloatingContentView(mContext, fwm));
            }
        });
    }
}
