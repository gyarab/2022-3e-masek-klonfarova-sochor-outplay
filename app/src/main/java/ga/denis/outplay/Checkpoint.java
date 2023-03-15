package ga.denis.outplay;

import android.location.Location;
import android.os.Handler;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.OutputStream;

public class Checkpoint {
    LatLng lokace;
    GoogleMap mMap;
    Marker me;
    //Marker hrac;
    String type;
    int time = 8;
    int cas;
    boolean capturing = false;
    int id;
    boolean capture = true;

    public Checkpoint(GoogleMap mMap, LatLng lokace/*, @Nullable Marker hrac*/, String type) {
        this.lokace = lokace;
        this.mMap = mMap;
        this.type = type;
        //this.hrac = hrac;
        me = mMap.addMarker(new MarkerOptions().position(lokace).title("Checkpoint").icon(BitmapDescriptorFactory.fromAsset("checkpoint.bmp")).flat(true).anchor(0.5f,0.5f).draggable(true));
    }

    public Checkpoint(GoogleMap mMap, LatLng poly1, LatLng poly2, LatLng poly3, LatLng poly4, Double x, Double y/*, @Nullable Marker hrac*/, String type) {
        this.mMap = mMap;
        this.type = type;
        //this.hrac = hrac;
        //this.lokace = new LatLng((poly1.latitude + ((poly2.latitude - poly1.latitude) * y / 100)) + (y / 100 * ((poly4.latitude + ((poly3.latitude - poly4.latitude) * y / 100)) - (poly1.latitude + ((poly2.latitude - poly1.latitude) * y / 100)))), (poly1.longitude + ((poly2.longitude - poly1.longitude) * x / 100)) + (x / 100 * ((poly4.longitude + ((poly3.longitude - poly4.longitude) * x / 100)) - (poly1.longitude + ((poly2.longitude - poly1.longitude) * x / 100)))));
        //this.lokace = new LatLng((((poly1.longitude * (100 - y) + poly4.longitude * y) / 100) * (100 - x) + ((poly2.longitude * (100 - y) + poly3.longitude * y) / 100) * x) / 100, (((poly1.latitude * (100 - x) + poly2.latitude * x) / 100) * (100 - y) + ((poly4.latitude * (100 - x) + poly3.latitude * x) / 100) * y) / 100);
        this.lokace = new LatLng((((poly1.latitude * (100 - y) + poly4.latitude * y) / 100) * (100 - x) + ((poly2.latitude * (100 - y) + poly3.latitude * y) / 100) * x) / 100, (((poly1.longitude * (100 - x) + poly2.longitude * x) / 100) * (100 - y) + ((poly4.longitude * (100 - x) + poly3.longitude * x) / 100) * y) / 100);
        me = mMap.addMarker(new MarkerOptions().position(lokace).title("Checkpoint").icon(BitmapDescriptorFactory.fromAsset("checkpoint.bmp")).flat(true).anchor(0.5f,0.5f).draggable(true));
    }

    public void setTime(int cas) {
        if (type.equals("time")) {
            if (cas > 0) time = cas;
            else System.out.println("Time value must be higher than zero");
        } else {
            System.out.println("Can't set the time property of a non-time checkpoint");
        }
    }

    public boolean inside(LatLng hrac) {
        boolean a = false;

        float[] results = new float[1];
        Location.distanceBetween(me.getPosition().latitude, me.getPosition().longitude, hrac.latitude, hrac.longitude, results);
        if(results[0] <= 15) {
            a = true;
            if (!capturing) me.setIcon(BitmapDescriptorFactory.fromAsset("checkpoint_entered.bmp"));
        } else {
            a = false;
            if (!capturing) me.setIcon(BitmapDescriptorFactory.fromAsset("checkpoint_uncaptured.bmp"));
        }

        return a;
    }

    public LatLng getLocation() {
        return lokace;
    }

    public void capture(int id) {
        if (type.equals("time")) {
            capturing = true;
            cas = time;
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                public void run() {
                    if (cas == 0) {
                        me.setIcon(BitmapDescriptorFactory.fromAsset("checkpoint.bmp"));
                        send("finishcap_" + id);
                        GameplayActivity.publicHrac.capture = true;
                        capture = false;
                    }
                    else if (inside(GameplayActivity.publicHrac.location)) {
                        cas--;
                        System.out.println(Math.floor(cas / (time / 8d)));
                        me.setIcon(BitmapDescriptorFactory.fromAsset("checkpoint_" + (int) Math.floor(cas / (time / 8d)) + ".bmp"));
                        handler.postDelayed(this, 1000);
                    } else {
                        capturing = false;
                        send("stopcap_" + id);
                        GameplayActivity.publicHrac.capture = true;
                    }
                }
            };
            runnable.run();
        }
    }

    private void send(String message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream output = null;
                try {
                    output = SocketHandler.getSocket().getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    output.write(message.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
