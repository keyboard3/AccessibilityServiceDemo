package com.keyboard3.accessibilityservicedemo;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.util.Utils;

/**
 * Created by keyboard3 on 2018/1/25.
 */

public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Utils.init(this);
    }
}
