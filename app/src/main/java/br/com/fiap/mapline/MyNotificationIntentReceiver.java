package br.com.fiap.mapline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

public class MyNotificationIntentReceiver extends BroadcastReceiver {
    public MyNotificationIntentReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        System.out.println("RECIEVEDDDDDDDDDDDDDDD");
        if (intent.getExtras().getInt("REQUEST_CODE", 0) == 49812) {

            EventBus.getDefault().post(new OnNotificationRemoved());
        }
    }
}
