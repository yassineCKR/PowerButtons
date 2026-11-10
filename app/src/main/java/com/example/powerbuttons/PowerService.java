package com.example.powerbuttons;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;

public class PowerService extends AccessibilityService {

    static PowerService instance;

    // Words to look for on the power menu (English, French, Arabic, Spanish, German)
    static final String[] RESTART_WORDS = {
            "restart", "reboot", "redémarrer", "redemarrer",
            "إعادة التشغيل", "إعادة تشغيل", "reiniciar", "neu starten"
    };
    static final String[] SHUTDOWN_WORDS = {
            "power off", "shut down", "shutdown", "éteindre", "eteindre",
            "إيقاف التشغيل", "إطفاء", "apagar", "ausschalten", "herunterfahren"
    };

    private String[] targets = null;
    private long deadline = 0;
    private long lastClick = 0;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        instance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onInterrupt() { }

    /** Opens the power menu and tries to tap the matching option. */
    void trigger(String[] words) {
        targets = words;
        deadline = System.currentTimeMillis() + 6000;
        lastClick = 0;
        performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (targets == null) return;
        long now = System.currentTimeMillis();
        if (now > deadline) {
            targets = null;
            return;
        }
        if (now - lastClick < 700) return;

        boolean clicked = false;

        List<AccessibilityWindowInfo> windows = getWindows();
        if (windows != null) {
            for (AccessibilityWindowInfo w : windows) {
                AccessibilityNodeInfo root = w.getRoot();
                if (root != null && tryClick(root)) {
                    clicked = true;
                    break;
                }
            }
        }
        if (!clicked) {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) clicked = tryClick(root);
        }

        if (clicked) {
            lastClick = now;
            // Stay active briefly in case the phone asks for a confirmation tap.
            deadline = now + 3000;
        }
    }

    private boolean tryClick(AccessibilityNodeInfo node) {
        if (node == null) return false;
        if (getPackageName().contentEquals(node.getPackageName() == null ? "" : node.getPackageName())) {
            return false;
        }

        if (matches(node)) {
            AccessibilityNodeInfo n = node;
            while (n != null) {
                if (n.isClickable() && n.isEnabled()) {
                    return n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                n = n.getParent();
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (tryClick(node.getChild(i))) return true;
        }
        return false;
    }

    private boolean matches(AccessibilityNodeInfo node) {
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String s = ((text == null ? "" : text.toString()) + " "
                + (desc == null ? "" : desc.toString())).toLowerCase();
        if (s.trim().isEmpty()) return false;
        for (String t : targets) {
            if (s.contains(t.toLowerCase())) return true;
        }
        return false;
    }
}
