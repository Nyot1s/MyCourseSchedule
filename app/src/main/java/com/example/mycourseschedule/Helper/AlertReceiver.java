package com.example.mycourseschedule.Helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.mycourseschedule.R;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel (required for Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("course_alerts", "Course Alerts", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "course_alerts")
                .setSmallIcon(R.drawable.warning_sign)
                .setContentTitle("Course Alert")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}