package br.com.fiap.pizza.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.firebase.client.Firebase;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by VyMajoriss on 5/18/2016.
 */
public class MyFirebaseUtil {

    static OnMyFirebaseReady mCallback;
    public static Random rnd = new Random();
    public static int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    public static SharedPreferences mPrefs;
    public static Firebase fireRef = new Firebase("https://torrid-fire-6287.firebaseio.com");
    public static Firebase mapRef = fireRef.child("map");
    public static Firebase myPolylineRef;
    public static List<Firebase> myLatLngRefList;
    public static String polylineId;

    public static void init(Context context) {

        try {
            mCallback = (OnMyFirebaseReady) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }

        mPrefs = context.getSharedPreferences("Google_firebase", context.MODE_PRIVATE);
        if (mPrefs.getString("myPolylineRefKey", null) == null) {

            mPrefs.edit().putInt("color", color).apply();

            myPolylineRef = mapRef.push();
            mPrefs.edit().putString("myPolylineRefKey", myPolylineRef.getKey()).apply();

        } else {
            color = mPrefs.getInt("color", 0);

            myPolylineRef = mapRef.child(mPrefs.getString("myPolylineRefKey", null));
            Set<String> myLatLngRefKeySet = mPrefs.getStringSet("myLatLngRefKeySet", null);
            if (myLatLngRefKeySet != null) {
                for (String key : myLatLngRefKeySet) {

                    myLatLngRefList.add(myPolylineRef.child(key));
                }
            }
        }

        polylineId = myPolylineRef.getKey();

        mCallback.onMyFireabseReady();

    }
    public interface OnMyFirebaseReady{

         void onMyFireabseReady();
    }

}
