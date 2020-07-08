package org.apache.cordova.callnotification;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.content.Context;

public class ReceveingCallActivity extends Activity  {

    private Bundle extras = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String package_name = getApplication().getPackageName();
        Resources res = getApplication().getResources();
        extras = getIntent().getExtras();

        setContentView(res.getIdentifier("activity_receveing_call", "layout", package_name));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        //KeyguardManager keyguard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //keyguard.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        if (Build.VERSION.SDK_INT < 30) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        CallNotification.startVibration(this);
    }

    public void clickJoin(View view) {
        extras.putString("action", "join_call");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.parseInt(extras.getString("id")));

        CallNotification.sendActionToJS(extras, this);

        CallNotification.createMainActivy(this, false);

        finish();
    }

    public void clickRefuse(View view) {
        extras.putString("action", "refuse_call");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.parseInt(extras.getString("id")));

        CallNotification.sendActionToJS(extras, this);

        CallNotification.createMainActivy(this, true);

        finish();
    }
}
