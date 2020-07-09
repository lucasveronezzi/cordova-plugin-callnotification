package org.apache.cordova.callnotification;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.content.Context;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

public class ReceveingCallActivity extends Activity  {

    private Bundle extras = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String package_name = getApplication().getPackageName();
        Resources res = getApplication().getResources();
        extras = getIntent().getExtras();

        int flags = WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
          setShowWhenLocked(true);
          setTurnScreenOn(true);
        } else {
          flags = flags |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        }

        KeyguardManager keyguard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguard.requestDismissKeyguard(this, null);
        } else {
          flags = flags |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
        }

        if (Build.VERSION.SDK_INT < 30) {
          flags = flags | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }

        getWindow().addFlags(flags);

        setContentView(res.getIdentifier("activity_receveing_call", "layout", package_name));

        ImageView userIcon = (ImageView) findViewById(res.getIdentifier("userIcon", "id", package_name));

        Glide.with(userIcon.getContext())
          .asBitmap()
          .load("https://homolog.consaudeonline.com.br/img/user_default.png")
          .apply(RequestOptions
            .circleCropTransform()
            .placeholder(res.getIdentifier("icon_user_round", "drawable", package_name))
          )
          .into(new BitmapImageViewTarget(userIcon) {
            @Override
            protected void setResource(Bitmap resource) {
              RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(userIcon.getContext().getResources(), addBorder(resource, userIcon.getContext()));
              circularBitmapDrawable.setCircular(true);
              userIcon.setImageDrawable(circularBitmapDrawable);
            }
          });

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

    private static Bitmap addBorder(Bitmap resource, Context context) {
      if(resource == null) return resource;

      int w = resource.getWidth();
      int h = resource.getHeight();
      int radius = Math.min(h / 2, w / 2);
      Bitmap output = Bitmap.createBitmap(w + 8, h + 8, Bitmap.Config.ARGB_8888);
      Paint p = new Paint();
      p.setAntiAlias(true);
      Canvas c = new Canvas(output);
      c.drawARGB(0, 0, 0, 0);
      p.setStyle(Paint.Style.FILL);
      c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);
      p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
      c.drawBitmap(resource, 4, 4, p);
      p.setXfermode(null);
      p.setStyle(Paint.Style.STROKE);
      p.setColor(ContextCompat.getColor(context, android.R.color.white));
      p.setStrokeWidth(5);
      c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);
      return output;
    }
}
