package org.apache.cordova.callnotification;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.content.Context;
import android.widget.Button;

public class ReceveingCallActivity extends Activity  {

    private String package_name = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        package_name = getApplication().getPackageName();
        Resources res = getApplication().getResources();

        setContentView(res.getIdentifier("activity_receveing_call", "layout", package_name));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        KeyguardManager keyguard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguard.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        Button clickButton = (Button) findViewById(res.getIdentifier("testeButton", "id", package_name));

        //Button clickButton = (Button) findViewById(R.id.testeButton);
        clickButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              createMainActivy();

            }
        });
    }

    public void createMainActivy() {
      if(CallNotification.activityIsKiled()) {
        Log.d("myplugin", "create activity");

        CallNotification.bringToFront = true;

        Intent mainApp = this.getApplicationContext().getPackageManager().getLaunchIntentForPackage(package_name);

        mainApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(mainApp);

        finish();

      } else {
        CallNotification.showInLockScreen();

        Intent mainApp = this.getApplicationContext().getPackageManager().getLaunchIntentForPackage(package_name);

        mainApp.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT  | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(mainApp);

        finish();

        Log.d("myplugin", "show lock screen");
      }
    }
}
