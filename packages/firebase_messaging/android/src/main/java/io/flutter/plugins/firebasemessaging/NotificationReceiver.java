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
import com.google.firebase.functions.FirebaseFunctions;
import io.flutter.plugins.firebasemessaging.R;

public class NotificationReceiver extends BroadcastReceiver {
    static public String PENDING = "pending";
    static public String APPROVED = "approved";
    static public String REJECTED = "rejected";
    static public String CANCELLED = "cancelled";

    private void showApprovedNotification(Context context, String message, int notificationId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,
                FlutterFirebaseMessagingService.CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(message);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final int notificationId = Integer.parseInt(intent.getStringExtra("notificationId"));
        final String orderId = intent.getStringExtra("orderId");
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("orders").document(orderId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot orderDoc) {
                WriteBatch batch = null;
                Map<String, Object> orderData = orderDoc.getData();

                Map<String, Object> orderDetails = (Map<String, Object>) orderData.get("order");
                DocumentReference route = (DocumentReference) orderData.get("route");
                final String userRef = ((DocumentReference) orderData.get("user")).getPath().split("/")[1];
                String orderStatus = (String) orderDetails.get("status");
                final String menuNameEN = (String) orderDetails.get("menuNameEN");
                final String menuName = (String) orderDetails.get("menuName");

                if (PENDING.equals(orderStatus)) {
                    batch = db.batch();
                    batch.update(orderDoc.getReference(), new HashMap<String, Object>() {
                        {
                            put("order.status", APPROVED);
                        }
                    });
                    batch.update((DocumentReference) orderData.get("supplier"), new HashMap<String, Object>() {
                        {
                            put("pendingOrders", FieldValue.increment(-1));
                        }
                        {
                            put("approvedOrders", FieldValue.increment(1));
                        }
                    });
                    if (route != null) {
                        batch.update(route, new HashMap<String, Object>() {
                            {
                                put("pendingOrders", FieldValue.increment(-1));
                            }
                        });
                    }
                } else if (CANCELLED.equals(orderStatus)) {
                    showApprovedNotification(context, "Anulowane przez klienta: " + menuName, notificationId);
                } else if (REJECTED.equals(orderStatus)) {
                    batch = db.batch();
                    batch.update(orderDoc.getReference(), new HashMap<String, Object>() {
                        {
                            put("order.status", APPROVED);
                        }
                    });
                } else {
                    showApprovedNotification(context, "Odłożono: " + menuName, notificationId);
                }
                if (batch != null) {
                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showApprovedNotification(context, "Odłożono: " + menuName, notificationId);
                            Map<String, Object> pushNotificationMessage = new HashMap<>();
                            pushNotificationMessage.put("title", "Status zamówienia");
                            pushNotificationMessage.put("titleEN", "Order status");
                            pushNotificationMessage.put("userRef", userRef);
                            pushNotificationMessage.put("message", "Odłożono: " + menuName);
                            pushNotificationMessage.put("messageEN", "Approved: " + menuNameEN);
                            FirebaseFunctions.getInstance().getHttpsCallable("pushNotificationOrderStatus")
                                    .call(pushNotificationMessage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showApprovedNotification(context, "Operacja nie powiodła się", notificationId);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showApprovedNotification(context, "Operacja nie powiodła się", notificationId);
            }
        });
    }
}