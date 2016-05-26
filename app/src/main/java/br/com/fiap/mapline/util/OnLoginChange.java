package br.com.fiap.mapline.util;


import android.net.Uri;

public class OnLoginChange {
    public static boolean isLogged;
    public static String name;
    public static  String email;
    public static Uri avatar;


    public OnLoginChange(boolean isLogged, String name, String email, Uri avatar) {
        this.isLogged = isLogged;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
    }
}