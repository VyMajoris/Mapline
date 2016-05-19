package br.com.fiap.pizza.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import br.com.fiap.pizza.R;
import br.com.fiap.pizza.listeners.MyFabLastClickListener;
import br.com.fiap.pizza.listeners.MyMapRefChildEventListener;
import br.com.fiap.pizza.util.Details;
import br.com.fiap.pizza.util.MyFirebaseUtil;
import br.com.fiap.pizza.util.MyMapUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyMapFragment extends Fragment implements OnMapReadyCallback {

    OnMyMapReady mCallback;

    Random rnd = new Random();
    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    SharedPreferences mPrefs;
    PolylineOptions myPolylineOptions;
    GoogleMap mMap;
    Firebase mapRef;
    Firebase fireRef;
    String polylineId;
    Firebase myPolylineRef;
    Map<String, Object> listOfLines;
    List<Polyline> myPolilines = new ArrayList<>();
    FloatingActionMenu fam;
    View view;
    Marker myMarker = null;
    List<Firebase> myLatLngRefList = new ArrayList<>();


    public MyMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnMyMapReady) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_map, container, false);
        final SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        mPrefs = getActivity().getSharedPreferences("Google_firebase", getActivity().MODE_PRIVATE);

        this.setRetainInstance(true);
        mapFragment.getMapAsync(this);
        listOfLines = new HashMap<>();



        fireRef = new Firebase("https://torrid-fire-6287.firebaseio.com");
        mapRef = fireRef.child("map");


        if (mPrefs.getString("myPolylineRefKey", null) == null) {

            System.out.println("NULL COLOR: " + color);
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

        fam = (FloatingActionMenu) view.findViewById(R.id.fam);


        FloatingActionButton fabAll = (FloatingActionButton) view.findViewById(R.id.delete_all_button);
        fabAll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mPrefs.getBoolean("isLogged", false)) {
                    fam.close(true);
                    if (myPolilines.size() == 0) {
                        Snackbar.make(view, R.string.no_more_points, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
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
                } else {
                    Snackbar.make(view, R.string.login_needed, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        return view;
    }

    Firebase myLatLngRef;

    @Override
    public void onMapReady(final GoogleMap map) {
        myPolylineOptions = new Gson().fromJson(mPrefs.getString("myPolylineOptions", null), PolylineOptions.class);
        if (myPolylineOptions != null) {

            System.out.println("999999999999999999999999");
            myPolilines.add(map.addPolyline(myPolylineOptions));

            myMarker = map.addMarker(new MarkerOptions().position(MyMapUtil.getCenter(myPolylineOptions.getPoints())));

        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                fam.close(true);
                if (mPrefs.getBoolean("isLogged", false)) {
                    if (myPolylineOptions == null) {
                        myPolylineOptions = new PolylineOptions();
                        myPolylineOptions.color(color);
                    }

                    myLatLngRef = myPolylineRef.push();

                    if (myMarker != null) {
                        myMarker.remove();
                    }

                    myPolylineOptions.add(latLng);
                    myMarker = map.addMarker(new MarkerOptions().position(MyMapUtil.getCenter(myPolylineOptions.getPoints())));
                    myPolilines.add(map.addPolyline(myPolylineOptions));
                    myLatLngRefList.add(myLatLngRef);
                    Details details = new Details(color, mPrefs.getString("nome", ""), myMarker.getPosition(), mPrefs.getString("avatar", ""), mPrefs.getString("email", ""));
                    myPolylineRef.child("details").setValue(details);
                    myLatLngRef.setValue(new Gson().toJson(latLng));
                    Set<String> myLatLngJsonSet = new HashSet<String>();
                    myLatLngJsonSet.add(new Gson().toJson(latLng));
                    mPrefs.edit().putString("myPolylineOptions", new Gson().toJson(myPolylineOptions)).apply();
                    Set<String> myLatLngRefKeySet = new HashSet<String>();
                    for (Firebase latLngRef : myLatLngRefList) {
                        System.out.println("ADDING KEY: " + latLngRef);
                        myLatLngRefKeySet.add(latLngRef.getKey());
                    }
                    mPrefs.edit().putStringSet("myLatLngRefKeySet", myLatLngRefKeySet).apply();
                    mPrefs.edit().putString("myPolylineRefKey", myPolylineRef.getKey()).apply();
                } else {
                    Snackbar.make(view, R.string.login_needed, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        mapRef.addChildEventListener(new MyMapRefChildEventListener(polylineId, mapRef, map));
        mMap = map;
        final FloatingActionButton fabLast = (FloatingActionButton) view.findViewById(R.id.deletar_last_button);
        fabLast.setOnClickListener(new MyFabLastClickListener(fam, mPrefs, myPolilines, myMarker, myLatLngRefList, myPolylineOptions, map, color, myPolylineRef));
        mCallback.onFragmentInteraction();
    }


    @Override
    public void onPause() {
        super.onPause();
        System.out.println("ON PAUSE");
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("ON STOP");
    }

    @Override
    public void onResume() {
        super.onStop();
        System.out.println("ON RESUME");
    }

    @Override
    public void onDestroy() {
        System.out.println("On Destroy");
        super.onDestroy();
        if (mPrefs.getBoolean("isLogged", false)) {
            fam.close(true);
            if (myPolilines.size() != 0) {
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
            }
        }

    }


    public void updateMapCamera(LatLng latLng) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(4);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
    }


    public interface OnMyMapReady {
        // TODO: Update argument type and nome
        void onFragmentInteraction();
    }


}
