package com.example.powerbuttons;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);

        status = new TextView(this);
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        root.addView(status, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        Button reboot = makeButton("REBOOT", Color.parseColor("#1E88E5"));
        reboot.setOnClickListener(v -> run(PowerService.RESTART_WORDS));
        root.addView(reboot, params());

        Button shutdown = makeButton("SHUT DOWN", Color.parseColor("#E53935"));
        shutdown.setOnClickListener(v -> run(PowerService.SHUTDOWN_WORDS));
        root.addView(shutdown, params());

        Button settings = new Button(this);
        settings.setText("Open Accessibility settings");
        settings.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(settings, params());

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PowerService.instance != null) {
            status.setText("Service is ON. Ready.");
            status.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            status.setText("Service is OFF.\nTurn on \"Power Buttons\" in Accessibility settings.");
            status.setTextColor(Color.parseColor("#C62828"));
        }
    }

    private void run(String[] words) {
        if (PowerService.instance == null) {
            Toast.makeText(this, "Enable the accessibility service first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }
        PowerService.instance.trigger(words);
    }

    private Button makeButton(String label, int color) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(22);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(color);
        return b;
    }

    private LinearLayout.LayoutParams params() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 260);
        p.setMargins(0, 24, 0, 24);
        return p;
    }
}
