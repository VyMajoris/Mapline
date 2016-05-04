package br.com.fiap.pizza;

import android.content.Intent;

public class MessageEvent {
    static Intent data;

    static int requestCode;
    static int resultCode;
    public  MessageEvent(Intent data, int requestCode, int resultCode) {

        this.data = data;
        this.requestCode = requestCode;
        this.resultCode = resultCode;
    }



}