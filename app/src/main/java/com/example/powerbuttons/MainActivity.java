package com.example.powerbuttons;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int BG_TOP = 0xFF0F172A;
    private static final int BG_BOTTOM = 0xFF1E1B4B;
    private static final int TEXT_MUTED = 0xFF94A3B8;
    private static final int GREEN = 0xFF22C55E;
    private static final int RED = 0xFFEF4444;

    private float d;
    private TextView statusText;
    private GradientDrawable dotDrawable;
    private TextView enableLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        d = getResources().getDisplayMetrics().density;

        Window w = getWindow();
        w.setStatusBarColor(BG_TOP);
        w.setNavigationBarColor(BG_BOTTOM);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{BG_TOP, BG_BOTTOM}));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(48), dp(24), dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        // --- Logo badge ---
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_power);
        logo.setPadding(dp(22), dp(22), dp(22), dp(22));
        GradientDrawable logoBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{0xFF7C3AED, 0xFF0EA5E9});
        logoBg.setShape(GradientDrawable.OVAL);
        logo.setBackground(logoBg);
        logo.setElevation(dp(12));
        root.addView(logo, lp(dp(96), dp(96), 0, 0, 0, dp(20)));

        // --- Title ---
        TextView title = text("Power Buttons", 30, Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title, lp(-2, -2, 0, 0, 0, dp(6)));

        TextView sub = text("Reboot or shut down in one tap", 15, TEXT_MUTED, false);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub, lp(-2, -2, 0, 0, 0, dp(20)));

        // --- Status pill ---
        LinearLayout pill = new LinearLayout(this);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        pill.setPadding(dp(16), dp(8), dp(16), dp(8));
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(0xFF1E293B);
        pillBg.setCornerRadius(dp(40));
        pill.setBackground(pillBg);

        View dot = new View(this);
        dotDrawable = new GradientDrawable();
        dotDrawable.setShape(GradientDrawable.OVAL);
        dot.setBackground(dotDrawable);
        pill.addView(dot, lp(dp(10), dp(10), 0, 0, dp(10), 0));

        statusText = text("", 14, Color.WHITE, false);
        pill.addView(statusText, lp(-2, -2, 0, 0, 0, 0));
        root.addView(pill, lp(-2, -2, 0, 0, 0, dp(32)));

        // --- Main buttons ---
        root.addView(makeButton("Reboot", "Restart your phone",
                R.drawable.ic_restart, 0xFF0EA5E9, 0xFF6366F1,
                PowerService.RESTART_WORDS), lp(-1, dp(110), 0, 0, 0, dp(18)));

        root.addView(makeButton("Shut down", "Turn your phone off",
                R.drawable.ic_power, 0xFFF43F5E, 0xFFBE123C,
                PowerService.SHUTDOWN_WORDS), lp(-1, dp(110), 0, 0, 0, dp(28)));

        // --- Enable link ---
        enableLink = text("Open Accessibility settings", 14, 0xFF38BDF8, true);
        enableLink.setGravity(Gravity.CENTER);
        enableLink.setPadding(dp(16), dp(12), dp(16), dp(12));
        enableLink.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(enableLink, lp(-2, -2, 0, 0, 0, dp(8)));

        TextView tip = text("Tip: if the menu opens but nothing is tapped, tap the option yourself.",
                12, 0xFF64748B, false);
        tip.setGravity(Gravity.CENTER);
        root.addView(tip, lp(-1, -2, dp(12), 0, dp(12), 0));

        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean on = PowerService.instance != null;
        statusText.setText(on ? "Service active · ready" : "Service off · tap below to enable");
        dotDrawable.setColor(on ? GREEN : RED);
        enableLink.setVisibility(on ? View.GONE : View.VISIBLE);
    }

    private void run(String[] words) {
        if (PowerService.instance == null) {
            Toast.makeText(this, "Enable the accessibility service first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }
        PowerService.instance.trigger(words);
    }

    private View makeButton(String label, String sub, int iconRes,
                            int colorStart, int colorEnd, String[] words) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(24), 0, dp(24), 0);
        row.setClickable(true);
        row.setElevation(dp(8));

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorStart, colorEnd});
        bg.setCornerRadius(dp(28));
        row.setBackground(new RippleDrawable(
                ColorStateList.valueOf(0x55FFFFFF), bg, null));

        // icon in a translucent circle
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setPadding(dp(14), dp(14), dp(14), dp(14));
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(0x33FFFFFF);
        icon.setBackground(circle);
        row.addView(icon, lp(dp(60), dp(60), 0, 0, dp(18), 0));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(text(label, 22, Color.WHITE, true));
        texts.addView(text(sub, 13, 0xCCFFFFFF, false));
        row.addView(texts, lp(-2, -2, 0, 0, 0, 0));

        row.setOnClickListener(v -> run(words));

        // little press-down animation
        row.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(90).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                    break;
            }
            return false;
        });
        return row;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private LinearLayout.LayoutParams lp(int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(l, t, r, b);
        return p;
    }

    private int dp(int v) {
        return Math.round(v * d);
    }
}
