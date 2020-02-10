package io.flutter.plugins.firebasemessaging;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;
import androidx.core.app.NotificationCompat;
import io.flutter.plugin.common.MethodChannel;
import com.google.firebase.functions.FirebaseFunctions;
import io.flutter.plugins.firebasemessaging.R;

public class ContentNotificationReceiver extends BroadcastReceiver {
    public static MethodChannel channel;

    public static void setChannel(MethodChannel ch) {
        channel = ch;
    }

    private boolean sendMessageFromIntent(Intent intent) {
        final String supplierRef = intent.getStringExtra("supplierRef");
        Map<String, Object> message = new HashMap<>();

        Map<String, Object> notificationMap = new HashMap<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("supplierRef", supplierRef);

        message.put("notification", notificationMap);
        message.put("data", dataMap);
    
        channel.invokeMethod("onResume", message);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String  packageName = context.getPackageName();
        Intent  launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String  className = launchIntent.getComponent().getClassName();
        
        Intent i = new Intent();
        i.setClassName(packageName, className);
        sendMessageFromIntent(intent);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}