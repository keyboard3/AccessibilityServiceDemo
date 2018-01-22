package com.keyboard3.accessibilityservicedemo;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

import static android.view.View.FOCUS_DOWN;

/**
 * @author keyboard3 on 2018/1/21
 */

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    private static String detectKey = "";
    private static String value = "";
    private static int type = 0;
    private static boolean isSub = false;//二层处理
    private boolean startProxy = false;
    Handler handler = new Handler();

    @Subscribe
    public void changeDedectKey(MainActivity.OpenEvent event) {
        detectKey = event.key;
        value = event.value;
        type = event.type;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        EventBus.getDefault().unregister(this);
        return super.onUnbind(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            EventBus.getDefault().post(new FloatingWindow.ChangeEvent(event.getPackageName().toString(), event.getClassName().toString()));
        }
        if (TextUtils.isEmpty(detectKey)) {
            return;
        }
        Log.d(TAG, "===============packageName:" + event.getPackageName() + " className:" + event.getClassName() + " eventType:" + event.getEventType());
        List<AccessibilityNodeInfo> collection = null;
        if ((event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) && "com.android.settings".equals(event.getPackageName())) {
            /*
            //开启
            collection = event.getSource().findAccessibilityNodeInfosByText("Advanced options");
            if (type == 4 && !collection.isEmpty()) {
                collection.get(0).performAction(AccessibilityNodeInfo.ACTION_EXPAND);
                collection.get(1).performAction(AccessibilityNodeInfo.ACTION_EXPAND);
                startProxy = true;
                return;
            }
            if (type == 4 && startProxy && !event.getSource().findAccessibilityNodeInfosByText("Proxy").isEmpty()) {
                event.getSource().findAccessibilityNodeInfosByText("Proxy").get(0).focusSearch(View.FOCUS_DOWN).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return;
            }
            if (type == 4 && startProxy && !event.getSource().findAccessibilityNodeInfosByText("Manual").isEmpty()) {
                event.getSource().findAccessibilityNodeInfosByText("Manual").get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return;
            }
            */
            //wifi高级 dialog弹出 edit文本获取焦点
            findAll(event.getSource());
            String key = "SAVE";
            if ("zh".equals(Locale.getDefault().getLanguage())) {
                key = "保存";
            }
            collection = event.getSource().findAccessibilityNodeInfosByText(key);
            if (type == 4 && detectKey.equals("end") && !collection.isEmpty()) {
                for (AccessibilityNodeInfo item : collection) {
                    if (item.getClassName().toString().contains("Button")) {
                        item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        performGlobalAction(GLOBAL_ACTION_BACK);

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                performGlobalAction(GLOBAL_ACTION_BACK);
                                performGlobalAction(GLOBAL_ACTION_BACK);
                                performGlobalAction(GLOBAL_ACTION_BACK);
                                performGlobalAction(GLOBAL_ACTION_BACK);
                                performGlobalAction(GLOBAL_ACTION_BACK);
                                Toast.makeText(MyAccessibilityService.this, "设置代理成功", Toast.LENGTH_SHORT).show();
                            }
                        }, 100);
                        detectKey = "";
                        return;
                    }
                }
            }
            key = "Proxy hostname";
            if ("zh".equals(Locale.getDefault().getLanguage())) {
                key = "代理主机名";
            }
            collection = event.getSource().findAccessibilityNodeInfosByText(key);
            if (type == 4 && !detectKey.equals("end") && !collection.isEmpty()) {
                if (!TextUtils.isEmpty(value)) {
                    AccessibilityNodeInfo ipEdit = event.getSource().findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
                    //AccessibilityNodeInfo portEdit = event.getSource().focusSearch(FOCUS_DOWN);
                    clearNode(ipEdit);
                    pastNode(ipEdit, value);
                    /*clearNode(portEdit);
                    pastNode(portEdit, "8888");*/
                    detectKey = "end";
                }
                return;
            }
            if (isSub) {
                AccessibilityNodeInfo ListView;
                switch (type) {
                    case 2:
                        String overdrawSelectItem = "Show overdraw areas";
                        if ("zh".equals(Locale.getDefault().getLanguage())) {
                            overdrawSelectItem = "显示过度";
                        }
                        collection = event.getSource().findAccessibilityNodeInfosByText(overdrawSelectItem);
                        if (collection.size() <= 0) {
                            return;
                        }
                        ListView = collection.get(0).getParent();

                        if (ListView == null || ListView.getChildCount() < 3) {
                            return;
                        }
                        if (ListView.getChild(0).isChecked()) {
                            ListView.getChild(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else {
                            ListView.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                        detectKey = "";
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        isSub = false;
                        return;
                    case 3:
                        collection = event.getSource().findAccessibilityNodeInfosByText("adb shell dumpsys gfxinfo");
                        if (collection.size() <= 0) {
                            return;
                        }
                        ListView = collection.get(0).getParent();

                        if (ListView == null || ListView.getChildCount() < 3) {
                            return;
                        }
                        if (ListView.getChild(0).isChecked()) {
                            ListView.getChild(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else {
                            ListView.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                        detectKey = "";
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        isSub = false;
                        return;
                    case 4:
                        key = "Modify network";
                        if ("zh".equals(Locale.getDefault().getLanguage())) {
                            key = "管理网络设置";
                        }
                        collection = event.getSource().findAccessibilityNodeInfosByText(key);
                        if (collection.size() <= 0) {
                            return;
                        }
                        ListView = collection.get(0).getParent();
                        if (ListView == null) {
                            return;
                        }
                        ListView.getParent().getChild(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        isSub = false;
                        return;
                }
            }
            if (type == 4) {
                collection = event.getSource().findAccessibilityNodeInfosByText(detectKey);
                if (!collection.isEmpty()) {
                    if ("en".equals(Locale.getDefault().getLanguage())) {
                        collection.get(0).performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                    } else {
                        collection.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                    }
                    isSub = true;
                }
                return;
            }
            AccessibilityNodeInfo listViewNodeInfo = findNodeByClassName(event.getSource(), RecyclerView.class.getName(), ListView.class.getName());
            if (listViewNodeInfo != null) {
                //不断滚动
                collection = new ArrayList<>();
                boolean isNotEnd = true;
                while (collection.isEmpty() && isNotEnd) {
                    collection = listViewNodeInfo.findAccessibilityNodeInfosByText(detectKey);
                    if (collection.isEmpty()) {
                        Log.d(TAG, "没找到 移动");
                        isNotEnd = listViewNodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!collection.isEmpty()) {
                    AccessibilityNodeInfo selectNodeInfo = collection.get(0);
                    //找到了就拿到父view 然后从中 找到 按钮
                    AccessibilityNodeInfo parent = selectNodeInfo.getParent();
                    if (type == 1) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        detectKey = "";
                        performGlobalAction(GLOBAL_ACTION_BACK);
                    } else if (type == 2 || type == 3) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        isSub = true;
                    }
                } else {
                    Log.d(TAG, "最终没找到");
                }
                listViewNodeInfo.recycle();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void pastNode(AccessibilityNodeInfo ipEdit, String value) {
        Bundle arguments = new Bundle();
        arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
        arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                true);
        ipEdit.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                arguments);
        ipEdit.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        ClipData clip = ClipData.newPlainText("label", value);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clip);
        ipEdit.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void clearNode(AccessibilityNodeInfo edit) {
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
        edit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    public AccessibilityNodeInfo findNodeByClassName(AccessibilityNodeInfo nodeInfo, Object... array) {
        AccessibilityNodeInfo findNodeInfo = null;
        //找到listView
        Set<String> set = new HashSet<>();
        for (Object item : array) {
            set.add(item.toString());
        }
        Queue<AccessibilityNodeInfo> queue = new ArrayDeque<>();
        queue.add(nodeInfo);
        while (!queue.isEmpty()) {
            AccessibilityNodeInfo itemNodeInfo = queue.poll();
            if (itemNodeInfo == null) {
                continue;
            }
            Log.d(TAG, "itemNodeInfo.name:" + itemNodeInfo.getClassName());
            if (set.contains(itemNodeInfo.getClassName())) {
                findNodeInfo = itemNodeInfo;
            }
            for (int i = 0; i < itemNodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo item = itemNodeInfo.getChild(i);
                if (item != null) {
                    queue.offer(item);
                }
            }
        }
        return findNodeInfo;
    }

    public void findAll(AccessibilityNodeInfo nodeInfo) {
        Log.d(TAG, "----------开始遍历搜索----------");
        Queue<AccessibilityNodeInfo> queue = new ArrayDeque<>();
        queue.add(nodeInfo);
        while (!queue.isEmpty()) {
            AccessibilityNodeInfo itemNodeInfo = queue.poll();
            if (itemNodeInfo == null) {
                continue;
            }
            Log.d(TAG, "------迭代:" + itemNodeInfo.getClassName() + " content：" + itemNodeInfo.getText());
            for (int i = 0; i < itemNodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo item = itemNodeInfo.getChild(i);
                if (item != null) {
                    queue.offer(item);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}
