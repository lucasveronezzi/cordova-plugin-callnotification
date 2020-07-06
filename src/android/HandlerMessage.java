package org.apache.cordova.callnotification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.cordova.firebase.FirebasePluginHandlerInterface;

public class HandlerMessage implements FirebasePluginHandlerInterface {
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage, FirebaseMessagingService service) {
    Intent notifyIntent = new Intent(service, ReceveingCallActivity.class);
    // Set the Activity to start in a new, empty task
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    // Create the PendingIntent
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
      service, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
    );

    NotificationCompat.Builder builder = new NotificationCompat.Builder(service, "call");
    builder.setSmallIcon(service.getApplicationInfo().icon)
      .setContentTitle("chamada de video")
      .setContentText("teste")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setFullScreenIntent(notifyPendingIntent, true);

    NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(0, builder.build());
  }
}
