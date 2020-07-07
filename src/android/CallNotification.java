package org.apache.cordova.callnotification;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
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
          CallNotification.sendActionToJS(bundle);
        }
        CallNotification.notificationActionStack.clear();
      }
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

    public static void sendActionToJS(Bundle bundle) {
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

}
