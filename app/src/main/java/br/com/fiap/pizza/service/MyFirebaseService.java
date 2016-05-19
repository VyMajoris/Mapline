package br.com.fiap.pizza.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.fasterxml.jackson.databind.JsonNode;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import br.com.fiap.pizza.R;
import br.com.fiap.pizza.util.BitMapUtil;
import br.com.fiap.pizza.util.MyFirebaseMapUtil;

public class MyFirebaseService extends Service implements MyFirebaseMapUtil.OnMyFirebaseReady {

    private ChildEventListener listener;
    Firebase mapRef = null;
    Service mContext = this;
    private boolean readyToNotification = false;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        mapRef = new Firebase("https://torrid-fire-6287.firebaseio.com/map");


        mapRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (readyToNotification) {

                    GenericTypeIndicator<JsonNode> t = new GenericTypeIndicator<JsonNode>() {
                    };
                    JsonNode jsonNode = dataSnapshot.getValue(t);
                    JsonNode details = jsonNode.get("details");
                    JsonNode name = details.get("name");
                    JsonNode avatar = details.get("avatar");
                    Bitmap bitmap = BitMapUtil.getCircleBitmap(BitMapUtil.getBitmapFromURL(avatar.asText()));

                    if (!dataSnapshot.getKey().equals(MyFirebaseMapUtil.polylineId)) {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext).setSmallIcon(R.drawable.ic_account_circle).setContentTitle("Nova Linha Criada!").setContentText(name.textValue() + " Acabou de criar uma iniciar nova linha ").setLargeIcon(bitmap);

                        notificationManager.notify(1001, notificationBuilder.build());
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }

        });


        mapRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                readyToNotification = true;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }


    @Override
    public void onMyFireabseReady() {
        System.out.println("on MY FIREBASE READYHYYYYYYYYYYYYYYYYYYYYYY");

    }
}

