package org.apache.cordova.callnotification;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CallNotification extends CordovaPlugin {

    private static final String TAG = "CallNotificationPlugin";

    private static Activity cordovaActivity = null;

    public static boolean bringToFront = false;

    @Override
    protected void pluginInitialize() {
        cordovaActivity = this.cordova.getActivity();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
         try{
            if (action.equals("onActions")) {
                this.onActions(callbackContext);
                return true;
            }
        }catch(Exception e){
            handleExceptionWithContext(e, callbackContext);
        }
        return false;
    }

    @Override
    public void onStart() {
        if(bringToFront) {
            showInLockScreen();
        }
    }

    @Override
    public void onDestroy() {
        cordovaActivity = null;
        super.onDestroy();
    }

    protected static void handleExceptionWithContext(Exception e, CallbackContext context) {
        String msg = e.toString();
        Log.e(TAG, msg);
        context.error(msg);
    }

    private void onActions(final CallbackContext callbackContext) {
        JSONObject json = new JSONObject();

        PluginResult pluginresult = new PluginResult(PluginResult.Status.OK, json);
        pluginresult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginresult);
    }

    public static void showNotification(Context context) {
        Intent notifyIntent = new Intent(context, ReceveingCallActivity.class);
        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "call");
        builder.setSmallIcon(getApplicationInfo().icon)
            .setContentTitle("chamada de video")
            .setContentText("teste")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(notifyPendingIntent, true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    public static boolean activityIsKiled() {
      if (cordovaActivity == null) {
        return true;
      }
      return false;
    }

    public static void showInLockScreen() {
      Log.d("myplugin", "show lock screen");
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        cordovaActivity.setShowWhenLocked(true);
        cordovaActivity.setTurnScreenOn(true);
      }

      KeyguardManager keyguard = (KeyguardManager) cordovaActivity.getSystemService(Context.KEYGUARD_SERVICE);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // keyguard.requestDismissKeyguard(cordovaActivity, null);
      } else {
        cordovaActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
          WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
          WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
          WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
          WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
      }

      bringToFront = false;
    }

}
