package io.flutter.plugins.firebasemessaging;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;
import androidx.core.app.NotificationCompat;
import io.flutter.plugins.firebasemessaging.R;


public class NotificationReceiver extends BroadcastReceiver {
    static  public String PENDING = "pending";
    static  public String APPROVED = "approved";
    static  public String REJECTED = "rejected";
    static  public String CANCELLED = "cancelled";

    private void showApprovedNotification(Context context,String message, int notificationId){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, FlutterFirebaseMessagingService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(message);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    @Override
    public void onReceive(final Context context, Intent intent)  {
        final int notificationId = Integer.parseInt(intent.getStringExtra("notificationId"));
        final String orderId=intent.getStringExtra("orderId");
        final String menuName=intent.getStringExtra("menuName");
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        //TESt
        Map<String, Object> orderIDTEST = new HashMap<>();
        orderIDTEST.put("orderID", orderId);
        db.collection("test").document("test2").set(orderIDTEST);

        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot orderDoc) {
                        WriteBatch batch = db.batch();
                        Map<String,Object> orderData=orderDoc.getData();
                        //TEST
                        db.collection("test").document("test").set(orderData);

                        Map<String,Object> orderDetails= (Map<String,Object>) orderData.get("order");
                        String orderStatus = (String) orderDetails.get("status");
                        //TEST
                        orderIDTEST.put("status", orderStatus);
                        db.collection("test").document("test1").set(orderIDTEST);

                        if(PENDING.equals(orderStatus)){
                            batch.update(orderDoc.getReference(),new HashMap<String, Object>(){
                                        {put("order.status", APPROVED);}
                            });
                            batch.update((DocumentReference) orderData.get("supplier"), new HashMap<String, Object>(){
                                {put("pendingOrders", FieldValue.increment(-1));}
                            });
                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    showApprovedNotification(context,"Odłożono: "+ menuName,notificationId);
                                }
                            });
                        }
                        else if(CANCELLED.equals(orderStatus)){
                            showApprovedNotification(context, "Anulowane przez klienta: " + menuName, notificationId);
                        }else if(REJECTED.equals(orderStatus)){
                            batch.update(orderDoc.getReference(),new HashMap<String, Object>(){
                                {put("order.status", APPROVED);}
                            });
                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    showApprovedNotification(context, "Odłożono: " + menuName, notificationId);
                                }
                            });
                        }
                    }
                });
    }
}