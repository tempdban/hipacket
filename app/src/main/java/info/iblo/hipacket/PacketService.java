package info.iblo.hipacket;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class PacketService extends AccessibilityService {
    private final static String LOG_TAG = "hipacket";
    private boolean returnFlag = false;
    private int last_node = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(LOG_TAG,"-------------------------------------------------------------");
        int eventType = event.getEventType();
        Log.d(LOG_TAG,"packageName:" + event.getPackageName());
        Log.d(LOG_TAG,"source:" + event.getSource());
        Log.d(LOG_TAG,"source class:" + event.getClassName());
        Log.d(LOG_TAG,"event type(int):" + eventType);
        AccessibilityNodeInfo root = getRootInActiveWindow();
        switch (eventType) {


            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (returnFlag) {
                    performBack();
                    returnFlag = false;
                    break;
                }
                getLastPacket();
                break;

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();

                if (className.equals("com.baidu.hi.activities.Chat")) {
                    Log.i(LOG_TAG,"Detected Chat activity view, try to open envelope");
                    getLastPacket();
                } else if (className.equals("com.baidu.hi.luckymoney.LuckyMoneyActivity")) {
                    Log.i(LOG_TAG,"Detected LuckyMoney activity.");
                    List<AccessibilityNodeInfo> list1 =
                        root.findAccessibilityNodeInfosByText("查看我的红包记录");
                    List<AccessibilityNodeInfo> list2 =
                            root.findAccessibilityNodeInfosByText("手慢了，红包派完了");
                    if ((list1.size() + list2.size()) == 0) {
                        Log.i(LOG_TAG,"open envelope");
                        List<AccessibilityNodeInfo> list = root.findAccessibilityNodeInfosByText("拆红包");

                        AccessibilityNodeInfo parrent = null;
                        if (list.size() > 0) {
                            parrent = list.get(0);
                            while (parrent != null) {
                                if (parrent.isClickable()) {
                                    returnFlag = true;
                                    parrent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                                } else {
                                    parrent = parrent.getParent();
                                }
                            }
                        }
                        else {
                            list = root.findAccessibilityNodeInfosByViewId("com.baidu.hi:id/envelope_open");
                            if (list.size() > 0) {
                                parrent = list.get(0);
                                returnFlag = true;
                                parrent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }

                    } else {
                        performBack();
                    }
                }
                break;

        }
    }

    @Override
    public void onInterrupt() {

    }


    private void performBack() {
        Log.i(LOG_TAG,"Back");
        //clickAllViewId("com.baidu.hi:id/open_envelope_close_multi");
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    private void clickAllViewId(String viewId) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        List<AccessibilityNodeInfo> list =
                root.findAccessibilityNodeInfosByViewId(viewId);
        for (AccessibilityNodeInfo node : list) {
            Log.d(LOG_TAG, node.toString());
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private void clickAllText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        List<AccessibilityNodeInfo> list =
                root.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo node : list) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private void getLastPacket() {
        AccessibilityNodeInfo root = getRootInActiveWindow(), node = null;
        int last = 0;
        if (root != null) {
            List<AccessibilityNodeInfo> list =
                    root.findAccessibilityNodeInfosByViewId("com.baidu.hi:id/chat_item_left_lucky_money_content");
            list.addAll(root.findAccessibilityNodeInfosByViewId("com.baidu.hi:id/chat_item_right_lucky_money_content"));

            /*
            for (AccessibilityNodeInfo item : list) {
                item.getBoundsInScreen(loc);
                if (loc.bottom > last) {
                    last = loc.bottom;
                    node = item;
                }
            }*/

            if (list.size() > 0)
                node = list.get(list.size() - 1);

            if (node != null) {
                Rect loc = new Rect();
                node.getBoundsInScreen(loc);
                if (last_node != loc.bottom) {
                    last_node = loc.bottom;
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }



}
