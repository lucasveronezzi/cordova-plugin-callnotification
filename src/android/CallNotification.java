package org.apache.cordova.callnotification;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.util.Log;
import android.view.WindowManager;
import android.os.Build;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CallNotification extends CordovaPlugin {

    private static final String TAG = "CallNotificationPlugin";

    private static Activity cordovaActivity = null;

    public static boolean bringToFront = false;

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
