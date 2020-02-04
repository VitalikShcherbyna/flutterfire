package com.example.myapplication;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import androidx.core.app.NotificationCompat;
import io.flutter.plugins.firebasemessaging.R;


public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent)  {
        final int notificationId = Integer.parseInt(intent.getStringExtra("notificationId"));
        final String orderId=intent.getStringExtra("orderId");
        final String menuName=intent.getStringExtra("menuName");
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> city = new HashMap<>();
        city.put("action", "approved");
        db.collection("test").document("test")
                .set(city)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, FlutterFirebaseMessagingService.CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Zatwierdzono: "+ menuName + ' ' + orderId);
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(notificationId, mBuilder.build());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, FlutterFirebaseMessagingService.CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Błąd");
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(notificationId, mBuilder.build());
                    }
                });
    }
}