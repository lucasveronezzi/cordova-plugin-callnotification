package org.apache.cordova.callnotification;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class ReceveingCallActivity extends AppCompatActivity {

    private Bundle extras = null;

    private final int REQUEST_CODE_PERMISSIONS = 10;

    private final String[] REQUIRED_PERMISSIONS = new String[] { Manifest.permission.CAMERA };

    private LocalBroadcastManager mLocalBroadcastManager;


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
          if(intent.getAction().equals("org.apache.cordova.callnotification.activity.close")){
              CallNotification.stopVibration();
              CallNotification.stopRingtone();
              finish();
          }
      }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("org.apache.cordova.callnotification.activity.close");
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);

        String package_name = getApplication().getPackageName();
        Resources res = getApplication().getResources();
        extras = getIntent().getExtras();

        int flags = WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        if (Build.VERSION.SDK_INT < 30) {
          flags = flags | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }

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
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
        }

        getWindow().addFlags(flags);

        setContentView(res.getIdentifier("activity_receveing_call", "layout", package_name));

        ImageView userIcon = (ImageView) findViewById(res.getIdentifier("userIcon", "id", package_name));

        Glide.with(userIcon.getContext())
          .asBitmap()
          .load(extras.getString("userImg", ""))
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

        TextView textUserName = (TextView) findViewById(res.getIdentifier("calling_name", "id", package_name));
        textUserName.setText(extras.getString("user", ""));

        TextView textDescription = (TextView) findViewById(res.getIdentifier("description", "id", package_name));
        textDescription.setText(extras.getString("description", ""));

        CallNotification.startVibration(this);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
          ActivityCompat.requestPermissions(
              this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            }
        }
    }

    private boolean allPermissionsGranted() {
      return ContextCompat.checkSelfPermission(getBaseContext(), REQUIRED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
      ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

      cameraProviderFuture.addListener(
        new Runnable() {
            public void run() {
              // Preview
              Preview preview = new Preview.Builder().build();

              // Select back camera
              CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

              try {
                  ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                  // Unbind use cases before rebinding
                  cameraProvider.unbindAll();

                  PreviewView viewFinder = (PreviewView) findViewById(getApplication().getResources().getIdentifier("viewFinder", "id", getApplication().getPackageName()));

                  preview.setSurfaceProvider(viewFinder.createSurfaceProvider());

                  // Bind use cases to camera
                  Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)ReceveingCallActivity.this, cameraSelector, preview);
              } catch(Exception exc) {
                  Log.e("myplugin", "Use case binding failed", exc);
              }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }

    public void clickJoin(View view) {
        extras.putString("action", "join_call");

        HandlerMessage.clearNotification(Integer.parseInt(extras.getString("id")), this);

        CallNotification.sendActionToJS(extras, this);

        CallNotification.createMainActivy(this, false);

        finish();
    }

    public void clickRefuse(View view) {
        extras.putString("action", "refuse_call");

        HandlerMessage.clearNotification(Integer.parseInt(extras.getString("id")), this);

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
