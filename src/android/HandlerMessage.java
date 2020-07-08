package org.apache.cordova.callnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.cordova.firebase.FirebasePluginHandlerInterface;

import java.util.Map;

public class HandlerMessage implements FirebasePluginHandlerInterface {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage, FirebaseMessagingService service) {
        Map<String, String> data = remoteMessage.getData();

        Bundle bundle = new Bundle();
        for (String key : data.keySet()) {
          bundle.putString(key, data.get(key));
        }

        Context context = service.getApplicationContext();

        String title = "Chamada de Video";
        String body = "Você está recebendo uma chamada de video";
        int callId = 0;
        String package_name = context.getPackageName();
        Resources res = context.getResources();

        if(data.containsKey("title")) title = data.get("title");
        if(data.containsKey("body")) body = data.get("body");
        if(data.containsKey("id")) callId = Integer.parseInt(data.get("id"));

        Intent notifyIntent = new Intent(context, ReceveingCallActivity.class);
        notifyIntent.putExtras(bundle);
        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
          context, 1, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent joinButton = new Intent(context, JoinCallReceiver.class);
        joinButton.putExtras(bundle);
        PendingIntent joinPendingIntent = PendingIntent.getBroadcast(
          context, 5, joinButton, 0
        );

        Intent refuseButton = new Intent(context, RefuseCallReceiver.class);
        refuseButton.putExtras(bundle);
        PendingIntent refusePendingIntent = PendingIntent.getBroadcast(
          context, 6, refuseButton, 0
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "call");
        builder.setSmallIcon(res.getIdentifier("notification_icon", "mipmap", package_name))
          .setContentTitle(title)
          .setContentText(body)
          .setCategory(Notification.CATEGORY_CALL)
          .setOngoing(true)
          .setColor(ResourcesCompat.getColor(res, res.getIdentifier("accent", "color", package_name), null))
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
          .setFullScreenIntent(notifyPendingIntent, true)
          .addAction(android.R.drawable.ic_menu_call, "Atender",
            joinPendingIntent)
          .addAction(android.R.drawable.ic_menu_call, "Recusar",
            refusePendingIntent);

        NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(callId, builder.build());

        CallNotification.startVibration(context);
        CallNotification.startRingtone(context);
    }

    public static class JoinCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            extras.putString("action", "join_call");

            CallNotification.sendActionToJS(extras, context);

            CallNotification.createMainActivy(context, false);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(Integer.parseInt(extras.getString("id")));

            Log.d("myplugin", "Received join Event id: ");
        }
    }

    public static class RefuseCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            extras.putString("action", "refuse_call");

            CallNotification.sendActionToJS(extras, context);

            CallNotification.createMainActivy(context, true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(Integer.parseInt(extras.getString("id")));

            Log.d("myplugin", "Received refuse Event id: ");
        }
    }
}
