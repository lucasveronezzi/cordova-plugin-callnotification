package org.apache.cordova.callnotification;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.apache.cordova.firebase.FirebasePluginMessageReceiver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

private class HandlerFcmReceiver extends FirebasePluginMessageReceiver {
    @Override
    public boolean onMessageReceived(RemoteMessage remoteMessage, FirebaseMessagingService service){
        Log.d("HandlerFcmReceiver", "onMessageReceived");

        // try {
        //     Map<String, String> data = remoteMessage.getData();
        //     isHandled = inspectAndHandleMessageData(data);
        // }catch (Exception e){
        //     handleException("onMessageReceived", e);
        // }
        
        CallNotification.showNotification(service);

        return true;
    }

    @Override
    public boolean sendMessage(Bundle bundle){
        return false;
    }
}
