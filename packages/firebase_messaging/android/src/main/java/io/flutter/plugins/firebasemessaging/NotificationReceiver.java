package io.flutter.plugins.firebasemessaging;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.annotation.NonNull;
import java.util.Random;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import androidx.core.app.NotificationCompat;
import io.flutter.plugins.firebasemessaging.R;


public class NotificationReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "plugins.flutter.io/firebase_messaging_background";
    public static final int NOTIFICATION_ID = 200;
    @Override
    public void onReceive(final Context context, Intent intent)  {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> city = new HashMap<>();
        city.put("action", "approved");
        db.collection("test").document("test")
        .set(city)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {}
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Random rand = new Random();
                int notificationID = rand.nextInt((100000 - 0) + 1) + 0;
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Błąd");
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(notificationID, mBuilder.build());
            }
        });
    }
}