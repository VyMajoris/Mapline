package br.com.fiap.pizza;


import android.net.Uri;

public class OnLoginChange {
    static boolean isLogged;
    static String name;
    static  String email;
    static Uri avatar;


    public OnLoginChange(boolean isLogged, String name, String email, Uri avatar) {
        this.isLogged = isLogged;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
    }
}