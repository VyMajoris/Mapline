package br.com.fiap.mapline.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import br.com.fiap.mapline.util.OnNotificationRemoved;

public class MyNotificationIntentReceiver extends BroadcastReceiver {
    public MyNotificationIntentReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras().getInt("REQUEST_CODE", 0) == 49812) {
            EventBus.getDefault().post(new OnNotificationRemoved());
        }
    }
}
