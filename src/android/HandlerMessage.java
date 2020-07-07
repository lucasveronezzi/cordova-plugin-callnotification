package org.apache.cordova.callnotification;

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

        String title = "Chamada de Video";
        String body = "Você está recebendo uma chamada de video";
        String package_name = service.getPackageName();
        Resources res = service.getResources();

        if(data.containsKey("title")) title = data.get("title");
        if(data.containsKey("body")) body = data.get("body");

        Intent notifyIntent = new Intent(service, ReceveingCallActivity.class);
        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
          service, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent joinButton = new Intent(service, JoinCallReceiver.class);
        joinButton.putExtras(bundle);
        PendingIntent joinPendingIntent = PendingIntent.getBroadcast(
          service, 5, joinButton, 0
        );

        Intent refuseButton = new Intent(service, RefuseCallReceiver.class);
        refuseButton.putExtras(bundle);
        PendingIntent refusePendingIntent = PendingIntent.getBroadcast(
          service, 6, refuseButton, 0
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, "call");
        builder.setSmallIcon(res.getIdentifier("notification_icon", "mipmap", package_name))
          .setContentTitle(title)
          .setContentText(body)
          .setOngoing(true)
          .setColor(ResourcesCompat.getColor(res, res.getIdentifier("accent", "color", package_name), null))
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setFullScreenIntent(notifyPendingIntent, true)
          .addAction(android.R.drawable.ic_menu_call, "Atender",
            joinPendingIntent)
          .addAction(android.R.drawable.ic_menu_call, "Recusar",
            refusePendingIntent);

        NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    public static class JoinCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            extras.putString("action", "join_call");
            CallNotification.sendActionToJS(extras);
            Log.d("myplugin", "Received join Event id: ");
        }
    }

    public static class RefuseCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            extras.putString("action", "refuse_call");
            CallNotification.sendActionToJS(extras);
            Log.d("myplugin", "Received refuse Event id: ");
        }
    }
}
