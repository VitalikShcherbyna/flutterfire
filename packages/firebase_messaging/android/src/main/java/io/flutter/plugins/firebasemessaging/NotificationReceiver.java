package io.flutter.plugins.firebasemessaging;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.FirebaseFirestore;


public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)  {
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
            public void onFailure(@NonNull Exception e) {}
        });
    }
}