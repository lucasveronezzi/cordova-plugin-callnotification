package org.apache.cordova.callnotification;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.os.Build;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

public class CallNotification extends CordovaPlugin {

    private static final String TAG = "CallNotificationPlugin";

    private static CallbackContext notificationActionCallbackContext;

    private static ArrayList<Bundle> notificationActionStack = null;

    private static Activity cordovaActivity = null;

    private static boolean bringToFront = false;

    private static Vibrator vibrate = null;

    private static Ringtone sound = null;

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
            } else if(action.equals("removeFromLockScreen")) {
               this.removeFromLockScreen(callbackContext);
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
    public void onReset() {
      CallNotification.notificationActionCallbackContext = null;
    }

    @Override
    public void onDestroy() {
        CallNotification.cordovaActivity = null;
        CallNotification.notificationActionCallbackContext = null;
        super.onDestroy();
    }

    protected static void handleExceptionWithContext(Exception e, CallbackContext context) {
        String msg = e.toString();
        Log.e(TAG, msg);
        context.error(msg);
    }

    private void onActions(final CallbackContext callbackContext) {
      CallNotification.notificationActionCallbackContext = callbackContext;
      if (CallNotification.notificationActionStack != null) {
        for (Bundle bundle : CallNotification.notificationActionStack) {
          CallNotification.sendActionToJS(bundle, cordovaActivity);
        }
        CallNotification.notificationActionStack.clear();
      }
    }

    private void removeFromLockScreen(final CallbackContext callbackContext) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        cordovaActivity.setShowWhenLocked(false);
        cordovaActivity.setTurnScreenOn(false);
      }

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        cordovaActivity.getWindow().clearFlags(
          WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
          WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
          WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
      }

      callbackContext.success();
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

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        cordovaActivity.getWindow().addFlags(
          WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
          WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
          WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
      }

      bringToFront = false;
    }

    public static void sendActionToJS(Bundle bundle, Context context) {
      stopVibration();
      stopRingtone();

      Log.d("myplugin", "receveid action");

      if (notificationActionCallbackContext == null) {
        if (CallNotification.notificationActionStack == null) {
          CallNotification.notificationActionStack = new ArrayList<Bundle>();
        }
        notificationActionStack.add(bundle);

        return;
      }

      JSONObject json = new JSONObject();
      Set<String> keys = bundle.keySet();
      for (String key : keys) {
        try {
          json.put(key, bundle.get(key));
        } catch (JSONException e) {
          handleExceptionWithContext(e, notificationActionCallbackContext);
          return;
        }
      }

      PluginResult pluginresult = new PluginResult(PluginResult.Status.OK, json);
      pluginresult.setKeepCallback(true);
      notificationActionCallbackContext.sendPluginResult(pluginresult);
    }

    public static void createMainActivy(Context context, boolean background) {
        String package_name = context.getApplicationContext().getPackageName();

        Intent mainApp = context.getApplicationContext().getPackageManager().getLaunchIntentForPackage(package_name);

        if(CallNotification.activityIsKiled()) {
            Log.d("myplugin", "create activity");

            //mainApp.putExtra("cdvStartInBackground", background);

            bringToFront = !background;

            mainApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  | Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(mainApp);

        } else {
            Log.d("myplugin", "show lock screen");

            if(!background) {
              CallNotification.showInLockScreen();

              mainApp.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

              mainApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

              context.startActivity(mainApp);
            }
        }
    }

    public static void startVibration(Context context) {
      if (vibrate == null) {
        vibrate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
      }

      long[] pattern = {0, 800, 1000};

      vibrate.cancel();

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        int[] mAmplitudes = new int[]{0, VibrationEffect.DEFAULT_AMPLITUDE, 0};
        vibrate.vibrate(VibrationEffect.createWaveform (pattern, mAmplitudes, 0));
      } else {
        //deprecated in API 26
        vibrate.vibrate(pattern, 0);
      }
    }

    public static void stopVibration() {
      if (vibrate != null) {
        vibrate.cancel();
      }
    }

    public static void startRingtone(Context context) {
      Uri path = Uri.parse("android.resource://" + context.getPackageName() + "/raw/receveing_call");
      RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, path);
      sound = RingtoneManager.getRingtone(context, path);
      sound.play();
    }

  public static void stopRingtone() {
    if(sound != null) {
      sound.stop();
    }
  }

}
