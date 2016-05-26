package br.com.fiap.mapline.util;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.ListIterator;

/**
 * Created by VyMajoriss on 5/18/2016.
 */
public class MyMapUtil {
    public static boolean removeLastLatLng(LatLng latLng, ListIterator it) {
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

    public static LatLng getCenter(List<LatLng> latLngs) {
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


}
