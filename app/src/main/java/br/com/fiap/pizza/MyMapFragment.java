package br.com.fiap.pizza;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
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
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyMapFragment extends Fragment implements OnMapReadyCallback {

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


            System.out.println("NULL REFFFF");
            myPolylineRef = mapRef.push();
            mPrefs.edit().putString("myPolylineRefKey", myPolylineRef.getKey()).apply();

        } else {
            myPolylineRef = mapRef.child(mPrefs.getString("myPolylineRefKey", null));
            Set<String> myLatLngRefKeySet = mPrefs.getStringSet("myLatLngRefKeySet", null);
            if (myLatLngRefKeySet != null) {
                for (String key : myLatLngRefKeySet) {
                    System.out.println("key------ "+key);
                    myLatLngRefList.add(myPolylineRef.child(key));
                }
            }
        }



        polylineId = myPolylineRef.getKey();

        fam = (FloatingActionMenu) view.findViewById(R.id.fam);

        FloatingActionButton fabMprefs = (FloatingActionButton) view.findViewById(R.id.clear_mPrefs_button);
        fabMprefs.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPrefs.edit().clear().apply();
            }
        });


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
                        mPrefs.edit().remove("myPolylineOptions").remove("myPolylineRefKey").apply();

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

            myMarker = map.addMarker(new MarkerOptions().position(getCenter(myPolylineOptions.getPoints())));

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


                    myMarker = map.addMarker(new MarkerOptions().position(getCenter(myPolylineOptions.getPoints())));
                    myPolilines.add(map.addPolyline(myPolylineOptions));


                    myLatLngRefList.add(myLatLngRef);

                    myPolylineRef.child("color").setValue(color);


                    myLatLngRef.setValue(new Gson().toJson(latLng));


                    Set<String> myLatLngJsonSet = new HashSet<String>();


                    myLatLngJsonSet.add(new Gson().toJson(latLng));
                    System.out.println("ADDING KEY: "+latLng);

                    mPrefs.edit().putString("myPolylineOptions", new Gson().toJson(myPolylineOptions)).apply();

                    Set<String> myLatLngRefKeySet = new HashSet<String>();
                    for (Firebase latLngRef : myLatLngRefList) {
                        System.out.println("ADDING KEY: "+latLngRef);
                        myLatLngRefKeySet.add(latLngRef.getKey());
                    }
                    mPrefs.edit().putStringSet("myLatLngRefKeySet", myLatLngRefKeySet).apply();


                } else {
                    Snackbar.make(view, R.string.login_needed, Snackbar.LENGTH_LONG).show();
                }

            }

        });

        mapRef.addChildEventListener(new ChildEventListener() {

            HashMap<String, PolylineOptions> polylineOptionsMap = new HashMap<>();
            HashMap<String, List<Polyline>> polylineHashMap = new HashMap<>();
            HashMap<String, List<LatLng>> latLngHashMap = new HashMap<>();
            HashMap<String, Integer> colorMap = new HashMap<>();
            HashMap<String, Marker> markerHashMap = new HashMap<String, Marker>();

            @Override
            public void onChildAdded(DataSnapshot snap, String s) {

                System.out.println(snap.getKey() + "___" + polylineId);

                if (!snap.getKey().equals(polylineId)) {
                    System.out.println(snap.getKey() + " ___ " + polylineId);
                    System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

                    polylineOptionsMap.put(snap.getKey(), new PolylineOptions());
                    polylineHashMap.put(snap.getKey(), new ArrayList<Polyline>());
                    latLngHashMap.put(snap.getKey(), new ArrayList<LatLng>());
                    markerHashMap.put(snap.getKey(), null);

                    snap.child("color").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            colorMap.put(dataSnapshot.getRef().getParent().getKey(), ((Long) dataSnapshot.getValue()).intValue());
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });


                    mapRef.child(snap.getKey()).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            String parentKey = dataSnapshot.getRef().getParent().getKey();
                            try {
                                if (!dataSnapshot.getKey().equals("color")) {

                                    if (markerHashMap.get(parentKey) != null) {
                                        markerHashMap.get(parentKey).remove();

                                    }

                                    LatLng latLng = new Gson().fromJson((String) dataSnapshot.getValue(), LatLng.class);
                                    polylineOptionsMap.put(parentKey, polylineOptionsMap.get(parentKey).color(colorMap.get(parentKey)).add(latLng));
                                    List<Polyline> polyTempArray = polylineHashMap.get(parentKey);
                                    polyTempArray.add(map.addPolyline(polylineOptionsMap.get(parentKey)));
                                    polylineHashMap.put(parentKey, polyTempArray);
                                    List<LatLng> latlngTempArray = latLngHashMap.get(parentKey);
                                    latlngTempArray.add(latLng);
                                    latLngHashMap.put(parentKey, latlngTempArray);
                                    markerHashMap.put(parentKey, map.addMarker(new MarkerOptions().position(getCenter(latLngHashMap.get(parentKey)))));
                                }

                            } catch (Exception e) {
                                System.out.println("ERROR KEY" + dataSnapshot.getKey());
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            //Removes last
                            String parentKey = dataSnapshot.getRef().getParent().getKey();
                            if (!parentKey.equals(polylineId) || !dataSnapshot.getKey().equals("color")) {
                                List<LatLng> latLngs = latLngHashMap.get(parentKey);
                                if (markerHashMap.get(parentKey) != null) {
                                    markerHashMap.get(parentKey).remove();
                                }

                                try {
                                    LatLng latLng = new Gson().fromJson((String) dataSnapshot.getValue(), LatLng.class);
                                    removeLastLatLng(latLng, latLngs.listIterator(latLngs.size()));

                                    for (Polyline polyline : polylineHashMap.get(parentKey)) {
                                        polyline.remove();
                                    }

                                    int color = polylineOptionsMap.get(parentKey).getColor();
                                    polylineOptionsMap.put(parentKey, new PolylineOptions().color(color).addAll(latLngs));
                                    List<Polyline> polyTempArray = polylineHashMap.get(parentKey);
                                    polyTempArray.add(map.addPolyline(polylineOptionsMap.get(parentKey)));
                                    polylineHashMap.put(parentKey, polyTempArray);

                                    if (!polylineOptionsMap.get(parentKey).getPoints().isEmpty()) {
                                        markerHashMap.put(parentKey, map.addMarker(new MarkerOptions().position(getCenter(latLngHashMap.get(parentKey)))));
                                    }

                                } catch (Exception e) {
                                    System.out.println("key type" + dataSnapshot.getKey());
                                }
                            }
                        }


                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });

                }
            }

            @Override
            public void onChildChanged(DataSnapshot snap, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.getKey().equals(polylineId) || dataSnapshot.getKey().equals("color")) {

                    for (Polyline polyline : polylineHashMap.get(dataSnapshot.getKey())) {
                        polyline.remove();
                    }
                    polylineOptionsMap.put(dataSnapshot.getKey(), new PolylineOptions());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        mMap = map;
        final FloatingActionButton fabLast = (FloatingActionButton) view.findViewById(R.id.deletar_last_button);
        fabLast.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View view) {
                if (mPrefs.getBoolean("isLogged", false)) {

                    for (Polyline line : myPolilines) {
                        line.remove();
                    }

                    myPolilines.clear();
                    if (myMarker != null) {
                        myMarker.remove();

                    }
                    if (!myLatLngRefList.isEmpty()) {
                        System.out.println("bbbbb" + myLatLngRefList);
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
                            myMarker = map.addMarker(new MarkerOptions().position(getCenter(myPolylineOptions.getPoints())));

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
                            mPrefs.edit().remove("myPolylineOptions").remove("myPolylineRefKey").apply();
                        }

                    } else {
                        fam.close(true);
                        Snackbar.make(view, R.string.no_more_points, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    fam.close(true);
                    Snackbar.make(view, R.string.login_needed, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
    }

    private boolean removeLastLatLng(LatLng latLng, ListIterator it) {
        if (latLng != null) {
            while (it.hasPrevious()) {
                if (latLng.equals(it.previous())) {
                    it.remove();
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }

    public LatLng getCenter(List<LatLng> latLngs) {
        double longitude = 0;
        double latitude = 0;
        double maxlat = 0, minlat = 0, maxlon = 0, minlon = 0;
        int i = 0;
        for (LatLng p : latLngs) {
            latitude = p.latitude;
            longitude = p.longitude;
            if (i == 0) {
                maxlat = latitude;
                minlat = latitude;
                maxlon = longitude;
                minlon = longitude;
            } else {
                if (maxlat < latitude) maxlat = latitude;
                if (minlat > latitude) minlat = latitude;
                if (maxlon < longitude) maxlon = longitude;
                if (minlon > longitude) minlon = longitude;
            }
            i++;
        }
        latitude = (maxlat + minlat) / 2;
        longitude = (maxlon + minlon) / 2;
        return new LatLng(latitude, longitude);
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


}
