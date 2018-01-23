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
    private static boolean closeSetting = false;
    private static String detectKey = "";
    private static String value = "";
    private static int type = 0;
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

    public class Config {
        public static final int AutoConfigInProxySpinner = 1;
        public static final int ProxyHostSpinnerInCommon = 2;
        public static final int ProxySpinnerNoneInSpinner = 3;
        public static final int ProxySpinnerManualInSpinner = 4;
        public static final int AdvancedOptionInAdvanceClose = 5;
        public static final int IPSettingsInCommon = 6;
        public static final int SaveButtonInWifiDailog = 7;
        public static final int ModifyWifiInConnectedLongClick = 8;
        public static final int OverDrawInDialog = 9;
        public static final int ProxyHostNameInAdvance = 10;
    }

    @Subscribe
    public void changeDedectKey(MainActivity.OpenEvent event) {
        detectKey = event.key;
        value = event.value;
        type = event.type;
        proxy_steps = 0;
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
        List<AccessibilityNodeInfo> collection = null;
        boolean isWifiActivity = "com.android.settings.Settings$WifiSettingsActivity".equals(event.getClassName());
        if (!"com.android.settings".equals(event.getPackageName())) {
            closeSetting = false;
            return;
        }
        if (closeSetting) {
            closePage();
        }
        findAll(rootNodeInfo);
        if ((event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)) {
            String key = "";
            //最后保存成功
            if (type == TYPE_PROXY) {
                boolean isClose = TextUtils.isEmpty(value);
                if (isWifiActivity && proxy_steps == 0) {
                    //代理已连接网络长按
                    proxyDirectClick(rootNodeInfo);
                    return;
                }
                if (isWifiActivity) return;
                if (proxy_steps == step_proxy_edit || proxy_steps == step_proxy_none) {
                    // 最后保存
                    proxySave(rootNodeInfo, isClose);
                    return;
                } else if (proxy_steps == step_wifi_click) {
                    //wifi已连接 管理修改选项
                    wifiManager_click(rootNodeInfo);
                    return;
                }
                if (!isClose) {
                    //没开启的设置开启
                    if (!wifiAdvanceOpen(rootNodeInfo)) {
                        //点开高级选项
                        if (proxy_steps == step_wifi_manager) {
                            proxyAdvanceClick(rootNodeInfo);
                        } else if (proxy_steps == step_proxy_advance) {
                            //点击None弹出选择框
                            proxyHostManualClick(rootNodeInfo, false);
                        } else if (proxy_steps == step_proxy_host) {
                            //选择框选择手动
                            proxyManualClick(rootNodeInfo);
                        }
                    } else if (proxy_steps == step_wifi_manager || proxy_steps == step_proxy_manual) {
                        //设置代理内容修改
                        proxyEdit(rootNodeInfo);
                    }
                } else {// 关闭代理开始
                    // 点击手动 弹出选择框
                    if ((proxy_steps == step_wifi_manager)) {
                        proxyHostManualClick(rootNodeInfo, true);
                    } else if (proxy_steps == step_proxy_host) {
                        // 弹出框显示 选择Wi-Fi关闭代理
                        if (!proxyNoneClick(rootNodeInfo)) {
                            proxyHostManualClick(rootNodeInfo, true);
                        }
                    }
                }
            }
            //弹出框选择 点击重新绘制
            if (proxy_steps == step_overdraw_click) {
                overdraw_selected(rootNodeInfo);
                return;
            } else if (proxy_steps == step_gpu_click) {
                //弹出框选择 点击GPU条形
                gpu_selected(rootNodeInfo);
            } else if (proxy_steps == 0) {
                nodeInfo = findNodeByClassName(rootNodeInfo, RecyclerView.class.getName(), ListView.class.getName());
                if (nodeInfo == null) return;

                collection = new ArrayList<>();
                //不断滚动找到制定项
                boolean isNotEnd = true;
                collection = listViewScrollToFind(collection, nodeInfo, isNotEnd);

                if (!collection.isEmpty()) {
                    AccessibilityNodeInfo selectNodeInfo = collection.get(0);
                    //找到了就拿到父view 然后从中 找到 按钮
                    AccessibilityNodeInfo parent = selectNodeInfo.getParent();
                    if (type == TYPE_LAYOUT) {
                        //点击边界显示
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        closePage();
                    } else if (type == TYPE_OVERDRAW) {
                        //点击重新绘制
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        proxy_steps = step_overdraw_click;
                    } else if (type == TYPE_GPU) {
                        //点击条形显示
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        proxy_steps = step_gpu_click;
                    }
                } else {
                    Toast.makeText(this, "没有找到该项", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void closePage() {
        closeSetting = true;
        performGlobalAction(GLOBAL_ACTION_BACK);
        performGlobalAction(GLOBAL_ACTION_BACK);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performGlobalAction(GLOBAL_ACTION_BACK);
                performGlobalAction(GLOBAL_ACTION_BACK);
            }
        }, 200);
        detectKey = "";
        type = 0;
        value = "";
        proxy_steps = 0;
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

    private boolean proxyManualClick(AccessibilityNodeInfo nodeInfo) {
        nodeInfo = getNodeByConfig(nodeInfo, Config.ProxySpinnerManualInSpinner);
        if (nodeInfo != null) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            proxy_steps = step_proxy_manual;
            return true;
        }
        return false;
    }

    /**
     * 点击代理服务器spinner 弹出选择手动开始none的代理方式
     *
     * @param rootNodeInfo
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean proxyHostManualClick(AccessibilityNodeInfo rootNodeInfo, boolean closeManual) {
        AccessibilityNodeInfo nodeInfo;
        nodeInfo = getNodeByConfig(rootNodeInfo, Config.ProxyHostSpinnerInCommon);
        if (nodeInfo != null) {
            if (closeManual) {
                nodeInfo = getNodeByConfig(rootNodeInfo, Config.ProxySpinnerManualInSpinner);
            } else {
                nodeInfo = getNodeByConfig(rootNodeInfo, Config.ProxySpinnerNoneInSpinner);
            }
            if (nodeInfo == null) return false;
            AccessibilityNodeInfo spinner = nodeInfo.getParent();
            spinner.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            proxy_steps = step_proxy_host;
            return true;
        }
        return false;
    }

    private AccessibilityNodeInfo getNodeByConfig(AccessibilityNodeInfo rootNodeInfo, int config) {
        String[] locals = {"", ""};
        switch (config) {
            case Config.AutoConfigInProxySpinner:
                locals = getCombineLocal("Proxy Auto-Config", "自动配置");
                break;
            case Config.ProxyHostNameInAdvance:
                locals = getCombineLocal("Proxy hostname", "代理主机名");
                break;
            case Config.ProxyHostSpinnerInCommon:
                locals = getCombineLocal("Proxy", "代理服务器");
                break;
            case Config.IPSettingsInCommon:
                locals = getCombineLocal("IP settings", "IP 设置");
                break;
            case Config.ProxySpinnerNoneInSpinner:
                locals = getCombineLocal("None", "无");
                break;
            case Config.ProxySpinnerManualInSpinner:
                locals = getCombineLocal("Manual", "手动");
                break;
            case Config.AdvancedOptionInAdvanceClose:
                locals = getCombineLocal("Advanced options", "高级选项");
                break;
            case Config.SaveButtonInWifiDailog:
                locals = getCombineLocal("SAVE", "保存");
                break;
            case Config.ModifyWifiInConnectedLongClick:
                locals = getCombineLocal("Modify network", "管理网络设置");
                break;
            case Config.OverDrawInDialog:
                locals = getCombineLocal("Show overdraw areas", "显示过度");
                break;
        }
        return getSingleInArray(rootNodeInfo.findAccessibilityNodeInfosByText(getStringByLocal(locals[0], locals[1])));
    }

    private String[] getCombineLocal(String en, String zh) {
        return new String[]{en, zh};
    }

    private <T> T getSingleInArray(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 将高级选项打开
     *
     * @param rootNodeInfo
     * @return
     */
    private boolean proxyAdvanceClick(AccessibilityNodeInfo rootNodeInfo) {
        rootNodeInfo = getNodeByConfig(rootNodeInfo, Config.AdvancedOptionInAdvanceClose);
        if (rootNodeInfo != null) {
            if (rootNodeInfo.isChecked()) return false;//已经被选中不用点击
            rootNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            proxy_steps = step_proxy_advance;
            return true;
        }
        return false;
    }

    private boolean wifiAdvanceOpen(AccessibilityNodeInfo rootNodeInfo) {
        return getNodeByConfig(rootNodeInfo, Config.ProxyHostNameInAdvance) != null;
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
            proxy_steps = step_wifi_click;
        }
    }

    /**
     * 代理主机和端口修改
     *
     * @param rootNodeInfo
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void proxyEdit(final AccessibilityNodeInfo rootNodeInfo) {
        AccessibilityNodeInfo ipEdit = rootNodeInfo.focusSearch(View.FOCUS_FORWARD);
        if (ipEdit == null) return;
        //代理主机修改
        while (!(ipEdit.getText().toString().contains("proxy.example.com") ||
                ipEdit.getText().toString().contains("192.168"))) {
            ipEdit = ipEdit.focusSearch(View.FOCUS_FORWARD);
            if (ipEdit == null || ipEdit.getText() == null) return;
        }
        clearNode(ipEdit);
        pastNode(ipEdit, value);
        //代理
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AccessibilityNodeInfo portEdit = rootNodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
                if (portEdit == null || portEdit.getText() == null) return;
                while (portEdit.getText().toString().contains("localhost") ||
                        portEdit.getText().toString().contains("192.168")) {
                    portEdit = portEdit.focusSearch(View.FOCUS_FORWARD);
                }
                clearNode(portEdit);
                pastNode(portEdit, "8888");
                proxy_steps = step_proxy_edit;
            }
        }, 100);
    }

    /**
     * 代理修改完成之后保存
     *
     * @param rootNodeInfo
     * @param isClose
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean proxySave(AccessibilityNodeInfo rootNodeInfo, final boolean isClose) {
        rootNodeInfo = getNodeByConfig(rootNodeInfo, Config.SaveButtonInWifiDailog);
        if (rootNodeInfo == null) return false;
        rootNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        Toast.makeText(MyAccessibilityService.this, isClose == true ? "关闭代理成功" : "设置代理成功", Toast.LENGTH_SHORT).show();
        closePage();
        return true;
    }

    /**
     * 进入wifi管理弹出框
     *
     * @param rootNodeInfo
     * @return
     */
    private boolean wifiManager_click(AccessibilityNodeInfo rootNodeInfo) {
        if (rootNodeInfo == null) return false;
        rootNodeInfo = getNodeByConfig(rootNodeInfo, Config.ModifyWifiInConnectedLongClick);
        if (rootNodeInfo == null) return false;
        rootNodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        proxy_steps = step_wifi_manager;
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean gpu_selected(AccessibilityNodeInfo rootNodeInfo) {
        List<AccessibilityNodeInfo> collection;
        collection = rootNodeInfo.findAccessibilityNodeInfosByText("adb shell dumpsys gfxinfo");
        if (collection.size() <= 0) return false;
        nodeInfo = collection.get(0).getParent();
        if (nodeInfo == null || nodeInfo.getChildCount() < 3) {
            return false;
        }
        if (nodeInfo.getChild(0).isChecked()) {
            //条形显示
            nodeInfo.getChild(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {//不显示
            nodeInfo.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        closePage();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean overdraw_selected(AccessibilityNodeInfo rootNodeInfo) {
        nodeInfo = getNodeByConfig(rootNodeInfo, Config.OverDrawInDialog);
        if (nodeInfo == null) return false;
        nodeInfo = nodeInfo.getParent();
        if (nodeInfo == null || nodeInfo.getChildCount() < 3) {
            return false;
        }
        if (nodeInfo.getChild(0).isChecked()) {
            nodeInfo.getChild(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            nodeInfo.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        closePage();
        return true;
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
