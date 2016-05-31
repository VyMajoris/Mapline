package br.com.fiap.mapline.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.fasterxml.jackson.databind.JsonNode;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import br.com.fiap.mapline.MyNotificationIntentReceiver;
import br.com.fiap.mapline.OnNotificationRemoved;
import br.com.fiap.mapline.R;
import br.com.fiap.mapline.util.BitMapUtil;
import br.com.fiap.mapline.util.MyFirebaseMapUtil;

public class MyFirebaseListenerService extends Service implements MyFirebaseMapUtil.OnMyFirebaseReady {

    Firebase mapRef = null;
    Service mContext = this;
    private boolean readyToNotification = false;

    ArrayList<String> names = new ArrayList<>();
    Bundle lastNotificationBundle;

    @Subscribe
    public void onMessageEvent(OnNotificationRemoved event) {
        names.clear();
    }



    @Override
    public IBinder onBind(Intent arg0) {
        EventBus.getDefault().register(this);
        return null;
    }

    private class NewLineNotificationTask extends AsyncTask<Bundle, Void, Void> {


        @Override
        protected Void doInBackground(Bundle... params) {
            Bitmap bitmap = BitMapUtil.getCircleBitmap(BitMapUtil.getBitmapFromURL(params[0].getString("avatar")));
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext);
            notificationBuilder.setTicker(params[0].getString("name ") + getString(R.string.new_line_notif_ticker));
            notificationBuilder.setSmallIcon(R.drawable.small_icon2);
            notificationBuilder.setContentTitle(getString(R.string.new_line_notif_title));
            notificationBuilder.setDeleteIntent(getDeleteIntent());
            notificationBuilder.setLargeIcon(bitmap);
            if (names.size() > 4) {
                inboxStyle.setSummaryText((names.size() - 4) + " " + getString(R.string.new_line_notif_line_4plus_summary));
            } else if (names.size() > 1) {
                inboxStyle.setSummaryText(names.size() + " " + getString(R.string.new_line_notif_1plus_summary));
            } else {
                inboxStyle.setSummaryText(names.size() + " " + getString(R.string.new_line_notif_one_summary));
            }


            for (int i = names.size() - 1; i >= 0; i--) {
                if (i <= 4) {

                    inboxStyle.addLine(Html.fromHtml(getString(R.string.new_line_notif_html_line, names.get(i))));
                }
            }
            notificationBuilder.setStyle(inboxStyle);

            String dataSnapKey = params[0].getString("dataSnapKey");
            if (dataSnapKey != null){
                if (!dataSnapKey.equals(MyFirebaseMapUtil.polylineId)) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(49812, notificationBuilder.build());

                }
            }
            return null;
        }
    }

    private class RemoveLineNotificationTask extends AsyncTask<Bundle, Void, Void> {


        @Override
        protected Void doInBackground(Bundle... params) {
            Bitmap bitmap = BitMapUtil.getCircleBitmap(BitMapUtil.getBitmapFromURL(params[0].getString("avatar")));

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                    .setTicker(params[0].getString("name") + " " + getString(R.string.remove_line_notif_ticker))
                    .setSmallIcon(R.drawable.small_icon2)
                    .setContentTitle(getString(R.string.remove_line_notif_title))
                    .setLargeIcon(bitmap)
                    .setContentText(params[0].getString("name") + " " + getString(R.string.removed_line_notif_text));


            String dataSnapKey = params[0].getString("dataSnapKey");
            if (dataSnapKey != null){
                if (!dataSnapKey.equals(MyFirebaseMapUtil.polylineId)) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(498120, notificationBuilder.build());

                }
            }
            return null;
        }
    }

    protected PendingIntent getDeleteIntent() {
        Intent intent = new Intent(mContext, MyNotificationIntentReceiver.class);
        intent.setAction("notification_cancelled");
        intent.putExtra("REQUEST_CODE", 49812);
        return PendingIntent.getBroadcast(mContext, 49812, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        EventBus.getDefault().register(this);
        mapRef = new Firebase(getString(R.string.fire_map_ref));


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


                    lastNotificationBundle = new Bundle();
                    lastNotificationBundle.putString("name", name.textValue());
                    lastNotificationBundle.putString("avatar", avatar.asText());
                    lastNotificationBundle.putString("dataSnapKey", dataSnapshot.getKey());


                    names.add(name.textValue());
                    new NewLineNotificationTask().execute(lastNotificationBundle);


                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (readyToNotification) {
                    GenericTypeIndicator<JsonNode> t = new GenericTypeIndicator<JsonNode>() {
                    };
                    JsonNode jsonNode = dataSnapshot.getValue(t);
                    JsonNode details = jsonNode.get("details");
                    JsonNode name = details.get("name");

                    JsonNode avatar = details.get("avatar");


                    Bundle removeBundle = new Bundle();
                    removeBundle.putString("name", name.textValue());
                    removeBundle.putString("avatar", avatar.asText());
                    removeBundle.putString("dataSnapKey", dataSnapshot.getKey());
                    names.remove(name.textValue());
                    new RemoveLineNotificationTask().execute(removeBundle);
                }
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
    }
}

