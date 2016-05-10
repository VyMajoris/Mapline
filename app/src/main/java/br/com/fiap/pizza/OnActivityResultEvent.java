package br.com.fiap.pizza;

import android.content.Intent;

public class OnActivityResultEvent {
    static Intent data;

    static int requestCode;
    static int resultCode;
    public OnActivityResultEvent(Intent data, int requestCode, int resultCode) {

        this.data = data;
        this.requestCode = requestCode;
        this.resultCode = resultCode;
    }



}