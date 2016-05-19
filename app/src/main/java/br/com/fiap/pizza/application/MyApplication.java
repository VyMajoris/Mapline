package br.com.fiap.pizza.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.firebase.client.Firebase;

import br.com.fiap.pizza.fragment.MyMapFragment;
import br.com.fiap.pizza.util.MyFirebaseUtil;

public class MyApplication extends Application implements MyFirebaseUtil.OnMyFirebaseReady{

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        Firebase.setAndroidContext(this);
        MyFirebaseUtil.init(this);
    }


    @Override
    public void onMyFireabseReady() {
        //start service

    }
}