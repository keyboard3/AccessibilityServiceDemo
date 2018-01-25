package com.keyboard3.accessibilityservicedemo.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by keyboard3 on 2018/1/23.
 */


public class PrefrenceUtil {

    private final String DB_NAME = "config";
    private static PrefrenceUtil prefrenceUtil = null;

    private Context context;
    private SharedPreferences preferences;


    /**
     * 存储内容，默认为 config
     *
     * @param context
     */
    private PrefrenceUtil(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(DB_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 存储内容
     *
     * @param context
     * @param dbName  存储文件名称
     */
    private PrefrenceUtil(Context context, String dbName) {
        this.context = context;
        preferences = context.getSharedPreferences(dbName, Context.MODE_PRIVATE);
    }

    /**
     * 实例化当前类
     *
     * @param context
     * @return
     */
    public static PrefrenceUtil getInstance(Context context) {
        if (prefrenceUtil == null)
            synchronized (PrefrenceUtil.class) {
                if (prefrenceUtil == null)
                    prefrenceUtil = new PrefrenceUtil(context);
            }
        return prefrenceUtil;
    }

    /**
     * 设置字符串
     *
     * @param key   存储key
     * @param value 存储的value
     */
    public String setString(String key, String value) {
        SharedPreferences.Editor sharedata = preferences.edit();
        sharedata.putString(key, value);
        sharedata.commit();
        return key;
    }


    /**
     * 获取存储的字符串
     *
     * @param key 存储的索引
     * @param def 获取的为空时，默认value
     * @return
     */
    public String getString(String key, String def) {
        return preferences.getString(key, def);
    }


    /**
     * 存储Boolean
     *
     * @param key   存储索引
     * @param value 存储的value
     */
    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor sharedata = preferences.edit();
        sharedata.putBoolean(key, value);
        sharedata.commit();
    }


    /**
     * 获取存储布尔值
     *
     * @param key 索引
     * @param def @null 设置成默认值
     * @return
     */
    public boolean getBoolean(String key, boolean def) {
        return preferences.getBoolean(key, def);
    }

}