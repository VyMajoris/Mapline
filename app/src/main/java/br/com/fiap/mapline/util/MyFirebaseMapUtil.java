package br.com.fiap.mapline.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by VyMajoriss on 5/18/2016.
 */
public class MyFirebaseMapUtil {

    static OnMyFirebaseReady mCallback;
    public static Random rnd = new Random();
    public static int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    public static Firebase mapRef = new Firebase("https://mapline-android.firebaseio.com/map");
    public static Firebase myPolylineRef;
    public static List<Firebase> myLatLngRefList = new ArrayList<>();
    public static String polylineId;
    public static List<Polyline> myPolilines = new ArrayList<>();
    public static SharedPreferences mPrefs;
    public static PolylineOptions myPolylineOptions;
    public static Firebase myLatLngRef;
    public static Marker myMarker = null;
    public static GoogleMap map;
    public static Context context;


    public static void init(Context context, SharedPreferences mPrefs) {
        MyFirebaseMapUtil.context = context;
        MyFirebaseMapUtil.mPrefs = mPrefs;


        try {
            mCallback = (OnMyFirebaseReady) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }

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

    public static void onMapReady(GoogleMap map) {
        MyFirebaseMapUtil.map = map;
        myPolylineOptions = new Gson().fromJson(mPrefs.getString("myPolylineOptions", null), PolylineOptions.class);
        if (myPolylineOptions != null) {
            myPolilines.add(MyFirebaseMapUtil.map.addPolyline(myPolylineOptions));
            myMarker = map.addMarker(new MarkerOptions().position(MyMapUtil.getCenter(myPolylineOptions.getPoints())).icon(BitmapDescriptorFactory.fromBitmap(BitMapUtil.getMarkerBitmapFromView(mPrefs.getString("avatar", null), context))));

        }
    }


    public static void addMyPoint(LatLng latLng) {

        if (myPolylineOptions == null) {
            myPolylineOptions = new PolylineOptions();
            myPolylineOptions.color(color);
        }

        myLatLngRef = MyFirebaseMapUtil.myPolylineRef.push();

        if (myMarker != null) {
            myMarker.remove();
        }

        myPolylineOptions.add(latLng);
        myMarker = map.addMarker(new MarkerOptions().position(MyMapUtil.getCenter(myPolylineOptions.getPoints())).icon(BitmapDescriptorFactory.fromBitmap(BitMapUtil.getMarkerBitmapFromView(mPrefs.getString("avatar", null), context))));


        myPolilines.add(map.addPolyline(myPolylineOptions));
        MyFirebaseMapUtil.myLatLngRefList.add(myLatLngRef);
        Details details = new Details(color, mPrefs.getString("nome", ""), myMarker.getPosition(), mPrefs.getString("avatar", ""), mPrefs.getString("email", ""));
        MyFirebaseMapUtil.myPolylineRef.child("details").setValue(details);
        myLatLngRef.setValue(new Gson().toJson(latLng));
        Set<String> myLatLngJsonSet = new HashSet<String>();
        myLatLngJsonSet.add(new Gson().toJson(latLng));
        mPrefs.edit().putString("myPolylineOptions", new Gson().toJson(myPolylineOptions)).apply();
        Set<String> myLatLngRefKeySet = new HashSet<String>();
        for (Firebase latLngRef : MyFirebaseMapUtil.myLatLngRefList) {
            System.out.println("ADDING KEY: " + latLngRef);
            myLatLngRefKeySet.add(latLngRef.getKey());
        }
        mPrefs.edit().putStringSet("myLatLngRefKeySet", myLatLngRefKeySet).putString("myPolylineRefKey", MyFirebaseMapUtil.myPolylineRef.getKey()).apply();


    }


    public static void removeLastMyPoint() {

        for (Polyline line : myPolilines) {
            line.remove();
        }

        myPolilines.clear();
        if (myMarker != null) {
            myMarker.remove();

        }

        List<LatLng> latLngs = myPolylineOptions.getPoints();

        if (latLngs.size() != 0) {
            latLngs.remove(latLngs.size() - 1);
        } else {
            latLngs.remove(latLngs.size());
        }


        myPolylineOptions = new PolylineOptions();
        myPolylineOptions.color(color);
        myPolylineOptions.addAll(latLngs);
        myPolilines.add(map.addPolyline(myPolylineOptions));


        if (latLngs.size() != 0) {
            myLatLngRefList.get(myLatLngRefList.size() - 1).removeValue();
            myLatLngRefList.remove(myLatLngRefList.size() - 1);
        } else {
            myLatLngRefList.get(0).removeValue();
            myLatLngRefList.remove(0);
        }

        if (myLatLngRefList.size() > 0) {
            myMarker = map.addMarker(new MarkerOptions().position(MyMapUtil.getCenter(myPolylineOptions.getPoints())).icon(BitmapDescriptorFactory.fromBitmap(BitMapUtil.getMarkerBitmapFromView(mPrefs.getString("avatar", null), context))));


            Details details = new Details(color, mPrefs.getString("nome", ""), myMarker.getPosition(), mPrefs.getString("avatar", ""), mPrefs.getString("email", ""));
            myPolylineRef.child("details").setValue(details);

        }


        Set<String> myLatLngJsonSet = new HashSet<String>();


        for (LatLng latlng : myPolylineOptions.getPoints()) {
            myLatLngJsonSet.add(new Gson().toJson(latlng));
        }

        mPrefs.edit().putString("myPolylineOptions", new Gson().toJson(myPolylineOptions)).apply();

        Set<String> myLatLngRefKeySet = new HashSet<String>();
        for (Firebase latLngRef : myLatLngRefList) {
            myLatLngRefKeySet.add(latLngRef.getKey());
        }
        mPrefs.edit().putStringSet("myLatLngRefKeySet", myLatLngRefKeySet).apply();

        if (myLatLngRefList.size() == 0) {
            myLatLngRefList.clear();
            myPolilines.clear();
            myPolylineRef.removeValue(null);
            mPrefs.edit().remove("myPolylineOptions").remove("myLatLngRefKeySet").remove("myPolylineRefKey").apply();
        }
    }

    public static void removeAllMyPoints() {

        myPolylineOptions = null;
        for (Polyline line : myPolilines) {
            line.remove();
        }
        if (myMarker != null) {
            myMarker.remove();
        }

        myLatLngRefList.clear();
        myPolilines.clear();
        myPolylineRef.removeValue(null);
        mPrefs.edit().remove("myPolylineOptions").remove("myPolylineRefKey").remove("myLatLngRefKeySet").apply();


    }


    public interface OnMyFirebaseReady {

        void onMyFireabseReady();
    }

}
