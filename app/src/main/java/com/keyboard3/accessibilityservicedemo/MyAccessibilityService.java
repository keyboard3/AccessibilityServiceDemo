package com.keyboard3.accessibilityservicedemo;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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

/**
 * @author keyboard3 on 2018/1/21
 */

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    private static String detectKey = "";
    private static int type = 0;
    private static boolean isSub = false;

    @Subscribe
    public void changeDedectKey(MainActivity.OpenEvent event) {
        detectKey = event.key;
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "packageName:" + event.getPackageName() + " eventType:" + event.getEventType());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            EventBus.getDefault().post(new FloatingWindow.ChangeEvent(event.getPackageName().toString(), event.getClassName().toString()));
        }
        if (TextUtils.isEmpty(detectKey)) {
            return;
        }
        if ((event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) && "com.android.settings".equals(event.getPackageName())) {
            Log.d(TAG, "AccessibilityNodeInfo:" + event.getSource().getClassName());
            if (isSub) {
                AccessibilityNodeInfo ListView;
                List<AccessibilityNodeInfo> collection;
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
                        String key = "Modify network";
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
                        detectKey = "";
                        isSub = false;
                        return;
                }
            }
            if (type == 4) {
                List<AccessibilityNodeInfo> collection = event.getSource().findAccessibilityNodeInfosByText(detectKey);
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
                List<AccessibilityNodeInfo> collection = new ArrayList<>();
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

    @Override
    public void onInterrupt() {

    }
}
