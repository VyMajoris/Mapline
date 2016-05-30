package br.com.fiap.mapline.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import br.com.fiap.mapline.R;
import br.com.fiap.mapline.listeners.MyMapRefChildEventListener;
import br.com.fiap.mapline.util.MyFirebaseMapUtil;


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
    Map<String, Object> listOfLines;
    List<Polyline> myPolilines = new ArrayList<>();
    FloatingActionMenu fam;
    View view;
    Marker myMarker = null;


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
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.mymap_fragment_title);
        final SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        mPrefs = getActivity().getSharedPreferences("Google_firebase", getActivity().MODE_PRIVATE);

        this.setRetainInstance(true);
        mapFragment.getMapAsync(this);
        listOfLines = new HashMap<>();
        fam = (FloatingActionMenu) view.findViewById(R.id.fam);
        FloatingActionButton fabAll = (FloatingActionButton) view.findViewById(R.id.delete_all_button);

        fabAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPrefs.getBoolean("isLogged", false)) {
                    fam.close(true);
                    if (MyFirebaseMapUtil.myPolilines.size() == 0) {
                        Snackbar.make(view, R.string.no_more_points, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
                        MyFirebaseMapUtil.removeAllMyPoints();
                    }
                } else {
                    Snackbar.make(view, R.string.login_needed, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        MyFirebaseMapUtil.onMapReady(map);
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {

                float minZoom = 5.0f;
                if (position.zoom < minZoom) {

                    map.animateCamera(CameraUpdateFactory.zoomTo(minZoom), 1000, null);
                }
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                fam.close(true);
                if (mPrefs.getBoolean("isLogged", false)) {

                    MyFirebaseMapUtil.addMyPoint(latLng);
                } else {
                    Snackbar.make(view, R.string.login_needed, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        MyFirebaseMapUtil.mapRef.addChildEventListener(new MyMapRefChildEventListener(getContext(), MyFirebaseMapUtil.polylineId, MyFirebaseMapUtil.mapRef, map));
        mMap = map;

        final FloatingActionButton fabLast = (FloatingActionButton) view.findViewById(R.id.deletar_last_button);
        // fabLast.setOnClickListener(new MyFabLastClickListener(fam, mPrefs, myPolilines, myMarker, MyFirebaseMapUtil.myLatLngRefList, myPolylineOptions, map, color, MyFirebaseMapUtil.myPolylineRef));

        fabLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPrefs.getBoolean("isLogged", false)) {
                    if (!MyFirebaseMapUtil.myLatLngRefList.isEmpty()) {
                        MyFirebaseMapUtil.removeLastMyPoint();
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
                MyFirebaseMapUtil.myLatLngRefList.clear();
                myPolilines.clear();
                MyFirebaseMapUtil.myPolylineRef.removeValue(null);
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
