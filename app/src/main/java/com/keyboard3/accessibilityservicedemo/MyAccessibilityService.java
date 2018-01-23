package com.keyboard3.accessibilityservicedemo;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ListView;
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
    public static final int TYPE_LAYOUT = 1;
    public static final int TYPE_OVERDRAW = 2;
    public static final int TYPE_GPU = 3;
    public static final int TYPE_PROXY = 4;
    private static String detectKey = "";
    private static String value = "";
    private static int type = 0;
    private static boolean isSub = false;//二层处理
    private boolean startProxy = false;
    private static int proxy_steps = 0;
    private final static int step_proxy_advance = 1;
    private final static int step_proxy_host = 2;
    private final static int step_proxy_manual = 3;
    private final static int step_proxy_none = 9;
    private final static int step_proxy_edit = 4;
    private final static int step_wifi_click = 5;
    private final static int step_wifi_manager = 8;
    private final static int step_overdraw_click = 6;
    private final static int step_gpu_click = 7;
    Handler handler = new Handler();
    private AccessibilityNodeInfo nodeInfo;

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
        final AccessibilityNodeInfo rootNodeInfo = event.getSource();
        Log.d(TAG, "===============packageName:" + event.getPackageName() + " className:" + event.getClassName() + " eventType:" + event.getEventType());
        List<AccessibilityNodeInfo> collection = null;
        if ((event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) && "com.android.settings".equals(event.getPackageName())) {
            String key = "";
            //最后保存成功
            findAll(rootNodeInfo);
            //代理已连接网络长按
            if (type == TYPE_PROXY && proxy_steps == 0) {
                proxyDirectClick(rootNodeInfo);
                return;
            }
            // 最后保存
            if (type == TYPE_PROXY && (proxy_steps == step_proxy_edit || proxy_steps == step_proxy_none)) {
                if (proxySave(rootNodeInfo)) return;
            }
            //关闭
            if (type == TYPE_PROXY && proxy_steps == step_wifi_manager && TextUtils.isEmpty(value)) {
                if (proxyHostManualClick(rootNodeInfo)) return;
            }
            if (type == TYPE_PROXY && proxy_steps == step_proxy_host && TextUtils.isEmpty(value)) {
                if (proxyNoneClick(rootNodeInfo)) return;
            }
            //开启
            if (type == TYPE_PROXY && proxy_steps == step_wifi_manager && !TextUtils.isEmpty(value)) {
                if (proxyAdvanceClick(rootNodeInfo)) return;
            }
            if (type == TYPE_PROXY && (proxy_steps == step_proxy_advance || proxy_steps == step_proxy_host) && !TextUtils.isEmpty(value)) {
                switch (proxy_steps) {
                    case step_proxy_advance:
                        if (proxyHostNoneClick(rootNodeInfo)) return;
                        break;
                    case step_proxy_host:
                        if (proxyManualClick(rootNodeInfo)) return;
                        break;
                }
            }
            //设置代理内容修改
            if (type == TYPE_PROXY && (proxy_steps == step_wifi_manager || proxy_steps == step_proxy_manual) && !TextUtils.isEmpty(value)) {
                nodeInfo = findSingleByContent(rootNodeInfo, "SAVE", "保存", null);
                if (nodeInfo == null) return;
                proxyEdit(rootNodeInfo);
                return;
            }
            if (proxy_steps == step_overdraw_click) {
                if (overdraw_selected(rootNodeInfo)) return;
            }
            if (proxy_steps == step_gpu_click) {
                if (gpu_selected(rootNodeInfo)) return;
            }
            if (proxy_steps == step_wifi_click) {
                if (wifiManager_click(rootNodeInfo)) return;
            }
            AccessibilityNodeInfo listViewNodeInfo = findNodeByClassName(rootNodeInfo, RecyclerView.class.getName(), ListView.class.getName());
            if (listViewNodeInfo != null) {
                collection = new ArrayList<>();
                boolean isNotEnd = true;
                collection = listViewScrollToFind(collection, listViewNodeInfo, isNotEnd);//不断滚动
                if (!collection.isEmpty()) {
                    AccessibilityNodeInfo selectNodeInfo = collection.get(0);
                    //找到了就拿到父view 然后从中 找到 按钮
                    AccessibilityNodeInfo parent = selectNodeInfo.getParent();
                    if (type == TYPE_LAYOUT) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        detectKey = "";
                        proxy_steps = 0;
                    } else if (type == TYPE_OVERDRAW || type == TYPE_GPU) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (type == TYPE_OVERDRAW) proxy_steps = step_overdraw_click;
                        if (type == TYPE_GPU) proxy_steps = step_gpu_click;
                    }
                } else {
                    Log.d(TAG, "最终没找到");
                }
                listViewNodeInfo.recycle();
            }
        }
    }

    private boolean proxyNoneClick(AccessibilityNodeInfo rootNodeInfo) {
        List<AccessibilityNodeInfo> collection;
        collection = rootNodeInfo.findAccessibilityNodeInfosByText(getStringByLocal("None", "无"));
        if (!collection.isEmpty()) {
            collection.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            proxy_steps = step_proxy_none;
            return true;
        }
        return false;
    }

    @NonNull
    private List<AccessibilityNodeInfo> listViewScrollToFind(List<AccessibilityNodeInfo> collection, AccessibilityNodeInfo listViewNodeInfo, boolean isNotEnd) {
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
        return collection;
    }

    private boolean proxyManualClick(AccessibilityNodeInfo rootNodeInfo) {
        List<AccessibilityNodeInfo> collection;
        collection = rootNodeInfo.findAccessibilityNodeInfosByText(getStringByLocal("Manual", "手动"));
        if (!collection.isEmpty()) {
            collection.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            proxy_steps = step_proxy_manual;
            return true;
        }
        return false;
    }

    private boolean proxyHostManualClick(AccessibilityNodeInfo rootNodeInfo) {
        List<AccessibilityNodeInfo> collection;
        collection = rootNodeInfo.findAccessibilityNodeInfosByText(getStringByLocal("Proxy hostname", "代理服务器"));
        if (!collection.isEmpty()) {
            collection = rootNodeInfo.findAccessibilityNodeInfosByText(getStringByLocal("Manual", "手动"));
            AccessibilityNodeInfo spinner = collection.get(0).getParent();
            spinner.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            proxy_steps = step_proxy_host;
            return true;
        }
        return false;
    }

    private boolean proxyHostNoneClick(AccessibilityNodeInfo rootNodeInfo) {
        List<AccessibilityNodeInfo> collection;
        collection = rootNodeInfo.findAccessibilityNodeInfosByText(getStringByLocal("Proxy hostname", "代理服务器"));
        if (!collection.isEmpty()) {
            collection = rootNodeInfo.findAccessibilityNodeInfosByText(getStringByLocal("None", "无"));
            AccessibilityNodeInfo spinner = collection.get(0).getParent();
            spinner.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            proxy_steps = step_proxy_host;
            return true;
        }
        return false;
    }

    private boolean proxyAdvanceClick(AccessibilityNodeInfo rootNodeInfo) {
        List<AccessibilityNodeInfo> collection;
        collection = rootNodeInfo.findAccessibilityNodeInfosByText(getStringByLocal("Advanced options", "高级选项"));
        if (!collection.isEmpty()) {
            if (collection.get(0).isChecked()) return false;//已经被选中不用点击
            collection.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            proxy_steps = 1;
            return true;
        }
        return false;
    }

    private void proxyDirectClick(AccessibilityNodeInfo rootNodeInfo) {
        List<AccessibilityNodeInfo> collection;
        collection = rootNodeInfo.findAccessibilityNodeInfosByText(detectKey);
        if (!collection.isEmpty()) {
            if (!isChinses()) {
                collection.get(0).performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
            } else {
                collection.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
            }
            isSub = true;
            proxy_steps = step_wifi_click;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void proxyEdit(final AccessibilityNodeInfo rootNodeInfo) {
        if (!TextUtils.isEmpty(value)) {
            AccessibilityNodeInfo ipEdit = rootNodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            clearNode(ipEdit);
            pastNode(ipEdit, value);

            AccessibilityNodeInfo portEdit = rootNodeInfo.focusSearch(View.FOCUS_BACKWARD);
            while (portEdit.getText() != null && portEdit.getText().toString().contains("localhost")) {
                portEdit = portEdit.focusSearch(View.FOCUS_BACKWARD);
            }
            clearNode(portEdit);
            pastNode(portEdit, "8888");

            proxy_steps = step_proxy_edit;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean proxySave(AccessibilityNodeInfo rootNodeInfo) {
        String key;
        List<AccessibilityNodeInfo> collection;
        key = getStringByLocal("SAVE", "保存");
        collection = rootNodeInfo.findAccessibilityNodeInfosByText(key);
        if (collection.isEmpty()) return true;
        for (AccessibilityNodeInfo item : collection) {
            if (item.getClassName().toString().contains("Button")) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                performGlobalAction(GLOBAL_ACTION_BACK);

                handler.postDelayed(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void run() {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        Toast.makeText(MyAccessibilityService.this, proxy_steps == step_proxy_none ? "关闭代理成功" : "设置代理成功", Toast.LENGTH_SHORT).show();
                    }
                }, 100);
                detectKey = "";
                proxy_steps = 0;
                type = 0;
                return true;
            }
        }
        return false;
    }

    private boolean wifiManager_click(AccessibilityNodeInfo rootNodeInfo) {
        nodeInfo = findSingleParentByContent(rootNodeInfo, "Modify network", "管理网络设置", null);
        if (nodeInfo == null) return true;
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        proxy_steps = step_wifi_manager;
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean gpu_selected(AccessibilityNodeInfo rootNodeInfo) {
        List<AccessibilityNodeInfo> collection;
        collection = rootNodeInfo.findAccessibilityNodeInfosByText("adb shell dumpsys gfxinfo");
        if (collection.size() <= 0) {
            return true;
        }
        nodeInfo = collection.get(0).getParent();

        if (nodeInfo == null || nodeInfo.getChildCount() < 3) {
            return true;
        }
        if (nodeInfo.getChild(0).isChecked()) {
            nodeInfo.getChild(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            nodeInfo.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        detectKey = "";
        proxy_steps = 0;
        performGlobalAction(GLOBAL_ACTION_BACK);
        performGlobalAction(GLOBAL_ACTION_BACK);
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean overdraw_selected(AccessibilityNodeInfo rootNodeInfo) {
        nodeInfo = findSingleParentByContent(rootNodeInfo, "Show overdraw areas", "显示过度", null);
        if (nodeInfo == null || nodeInfo.getChildCount() < 3) {
            return true;
        }
        if (nodeInfo.getChild(0).isChecked()) {
            nodeInfo.getChild(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            nodeInfo.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        proxy_steps = 0;
        detectKey = "";
        performGlobalAction(GLOBAL_ACTION_BACK);
        performGlobalAction(GLOBAL_ACTION_BACK);
        return false;
    }

    private AccessibilityNodeInfo findSingleParentByContent(AccessibilityNodeInfo event, String en, String zh, String className) {
        AccessibilityNodeInfo singleByContent = findSingleByContent(event, en, zh, className);
        if (singleByContent == null) return null;
        return singleByContent.getParent();
    }

    private AccessibilityNodeInfo findSingleByContent(AccessibilityNodeInfo event, String en, String zh, String className) {
        String key;
        List<AccessibilityNodeInfo> collection;
        key = getStringByLocal(en, zh);
        collection = event.findAccessibilityNodeInfosByText(key);
        if (!collection.isEmpty()) {
            if (!TextUtils.isEmpty(className)) {
                for (AccessibilityNodeInfo item :
                        collection) {
                    if (item.getClassName().equals(className)) {
                        return item;
                    }
                }
            } else {
                return collection.get(0);
            }
        }
        return null;
    }

    private int getIndexFormParent(AccessibilityNodeInfo accessibilityNodeInfo) {
        AccessibilityNodeInfo parent = accessibilityNodeInfo.getParent();
        if (parent == null) return -1;
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChild(i).equals(accessibilityNodeInfo)) {
                return i;
            }
        }
        return -1;
    }

    private AccessibilityNodeInfo getNextAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        int index = getIndexFormParent(accessibilityNodeInfo);
        if (index == -1) return null;
        return index + 1 > accessibilityNodeInfo.getParent().getChildCount() - 1 ? null : accessibilityNodeInfo.getParent().getChild(index + 1);
    }

    @NonNull
    private String getStringByLocal(String en, String zh) {
        String key = en;
        if (isChinses()) {
            key = zh;
        }
        return key;
    }

    private boolean isChinses() {
        return "zh".equals(Locale.getDefault().getLanguage());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void pastNode(AccessibilityNodeInfo ipEdit, String value) {
        if (ipEdit == null) return;
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
        if (edit == null) return;
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
        Queue<AccessibilityNodeInfo> queue = new ArrayDeque<>();
        queue.add(nodeInfo);
        while (!queue.isEmpty()) {
            AccessibilityNodeInfo itemNodeInfo = queue.poll();
            if (itemNodeInfo == null) {
                continue;
            }
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
