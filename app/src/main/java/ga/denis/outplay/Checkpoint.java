package ga.denis.outplay;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Checkpoint {
    LatLng lokace;
    GoogleMap mMap;
    Marker me;
    Marker hrac;

    public Checkpoint(GoogleMap mMap, LatLng lokace, Marker hrac) {
        this.lokace = lokace;
        this.mMap = mMap;
        this.hrac = hrac;
        me = mMap.addMarker(new MarkerOptions().position(lokace).title("Checkpoint").icon(BitmapDescriptorFactory.fromAsset("checkpoint.bmp")).flat(true).anchor(0.5f,0.5f).draggable(true));
    }

    public boolean inside() {
        boolean a = false;

        float[] results = new float[1];
        Location.distanceBetween(me.getPosition().latitude, me.getPosition().longitude, hrac.getPosition().latitude, hrac.getPosition().longitude, results);
        if(results[0] <= 15) {
            a = true;
            me.setIcon(BitmapDescriptorFactory.fromAsset("crosshair.bmp"));
        }

        return a;
    }
}
