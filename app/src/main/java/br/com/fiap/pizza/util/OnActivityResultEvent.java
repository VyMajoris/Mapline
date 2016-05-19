package br.com.fiap.pizza.util;

import android.content.Intent;

public class OnActivityResultEvent {
    public static  Intent data;

    public static int requestCode;
    public static int resultCode;
    public OnActivityResultEvent(Intent data, int requestCode, int resultCode) {

        this.data = data;
        this.requestCode = requestCode;
        this.resultCode = resultCode;
    }



}