package com.dirkarnez.singlepageappserver;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ToggleButton serverToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent serverIntent = new Intent(this, TinyWebServerService.class);

        serverToggle = findViewById(R.id.toggleButton);
        serverToggle.setChecked(isMyServiceRunning(TinyWebServerService.class));
        serverToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (serverToggle.isChecked()) {
                    startService(serverIntent);
                } else {
                    stopService(serverIntent);
                }
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            String name = serviceClass.getName();
            String serviceClassName = service.service.getClassName();
            if (name.equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }
}