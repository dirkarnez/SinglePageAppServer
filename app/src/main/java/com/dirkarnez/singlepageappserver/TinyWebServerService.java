package com.dirkarnez.singlepageappserver;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class TinyWebServerService extends Service {
    public static final String TAG = TinyWebServerService.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() executed");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, TAG)
                .setSmallIcon(R.drawable.notification_icon_background)
                .setContentTitle("Single Page App Server")
                .setContentText("Serving at 9000")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        startForeground(1, builder.build());

        new Thread(new Runnable() {
            @Override
            public void run() {
                TinyWebServer.startServer(9000,  Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/spa");
            }
        }).start();
   }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        Log.d(TAG, "onDestroy() executed");
        new Thread(new Runnable() {
            @Override
            public void run() {
                TinyWebServer.stopServer();
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}