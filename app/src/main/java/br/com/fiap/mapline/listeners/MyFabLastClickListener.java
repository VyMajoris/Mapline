package br.com.fiap.mapline.listeners;

import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.firebase.client.Firebase;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.fiap.mapline.R;
import br.com.fiap.mapline.util.Details;
import br.com.fiap.mapline.util.MyMapUtil;

/**
 * Created by VyMajoriss on 5/18/2016.
 */
public class MyFabLastClickListener implements  View.OnClickListener{


    SharedPreferences mPrefs;
    List<Polyline> myPolilines;
    Marker myMarker;
    List<Firebase> myLatLngRefList;
    PolylineOptions myPolylineOptions;
    GoogleMap map;
    int color;

    public MyFabLastClickListener(FloatingActionMenu fam, SharedPreferences mPrefs, List<Polyline> myPolilines, Marker myMarker, List<Firebase> myLatLngRefList, PolylineOptions myPolylineOptions, GoogleMap map, int color, Firebase myPolylineRef) {
        this.fam = fam;
        this.mPrefs = mPrefs;
        this.myPolilines = myPolilines;
        this.myMarker = myMarker;
        this.myLatLngRefList = myLatLngRefList;
        this.myPolylineOptions = myPolylineOptions;
        this.map = map;
        this.color = color;
        this.myPolylineRef = myPolylineRef;
    }

    Firebase myPolylineRef;
    FloatingActionMenu fam;

    @Override
    public void onClick(View view) {
        if (mPrefs.getBoolean("isLogged", false)) {


            if (!myLatLngRefList.isEmpty()) {

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
                    myMarker = map.addMarker(new MarkerOptions().position(MyMapUtil.getCenter(myPolylineOptions.getPoints())));

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

            } else {
                fam.close(true);
                Snackbar.make(view, R.string.no_more_points, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        } else {
            fam.close(true);
            Snackbar.make(view, R.string.login_needed, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

}
