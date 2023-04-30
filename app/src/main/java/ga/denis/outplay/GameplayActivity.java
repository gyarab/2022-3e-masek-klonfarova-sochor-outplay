package ga.denis.outplay;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import ga.denis.outplay.databinding.ActivityGameplayBinding;

public class GameplayActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityGameplayBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker hrac;
    //private boolean f = false;
    //TextView bearing;
    ArrayList<Checkpoint> checkList = new ArrayList<>();
    Location previous = null;
    Button interactButton;
    OutputStream output;
    BufferedReader bufferedReader;
    RelativeLayout relativeLayout;
    boolean sendChange = false;
    String change = "";
    int playerID;
    String[] teams = new String[4];
    LatLng[] locations = new LatLng[4];
    int eliminatable;
    int elimDist = 12;
    Marker nearby = null;
    TextView overlay;
    LatLng helperLokace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGameplayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //bearing = (TextView) findViewById(R.id.gameplayTextView);

        interactButton = (Button) findViewById(R.id.interactButton);
        interactButton.setOnClickListener(this);
        interactButton.setEnabled(false);

        overlay = findViewById(R.id.elimView);

        relativeLayout = findViewById(R.id.GameplayRelative);
        //overlay = findViewById(R.id.overlay);

        playerID = getIntent().getExtras().getInt("playerID");

        for (int i = 0; i < 4; i++) {
            teams[i] = getIntent().getExtras().getString("team" + (i + 1));
            System.out.println(teams[i]);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager()
                .findFragmentById(R.id.Gameplay);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Thread senderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    output = SocketHandler.getSocket().getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(SocketHandler.getSocket().getInputStream()));
                    if (bufferedReader != null) System.out.println("bufferedReader set");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                LatLng location = publicHrac.location;
                for (;;) {
                    if (location != publicHrac.location) {
                        location = publicHrac.location;
                        String lokace = "loc_" + location.latitude + "_" + location.longitude + "_" + playerID;
                        try {
                            output.write(lokace.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (sendChange) {
                        sendChange = false;
                        try {
                            output.write(change.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        senderThread.start();

        Thread recieverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("reciever thread started");
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(SocketHandler.getSocket().getInputStream()));
                    if (bufferedReader != null) System.out.println("bufferedReader set too");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String message = "";
                for (;;) {
                    try {
                        message = bufferedReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String[] divided = message.split("_");

                    if (divided[0].equals("loc")) {
                        System.out.println(divided[3]);
                        locations[Integer.parseInt(divided[3]) - 1] = (new LatLng(Double.parseDouble(divided[1]), Double.parseDouble(divided[2])));
                        System.out.println("Location of: " + divided[3] + " set to: " + locations[Integer.parseInt(divided[3]) - 1].longitude + " " + locations[Integer.parseInt(divided[3]) - 1].latitude);

                        if (teams[playerID - 1].equals("eliminate")) {
                            boolean elim = true;
                            for (int i = 0; i < locations.length; i++) {
                                if ((playerID - 1) != i && locations[i] != null && publicHrac.location != null && !teams[i].equals("eliminate")) {
                                    float[] results = new float[1];
                                    Location.distanceBetween(publicHrac.location.latitude, publicHrac.location.longitude, locations[i].latitude, locations[i].longitude, results);
                                    if (results[0] <= elimDist) {
                                        eliminatable = i + 1;
                                        elim = false;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                interactButton.setEnabled(true);
                                            }
                                        });

                                        helperLokace = locations[i];

                                        if (nearby != null) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    nearby.remove();
                                                    nearby = mMap.addMarker(new MarkerOptions().position(helperLokace).title("Nearby player").icon(BitmapDescriptorFactory.fromAsset("ping.bmp")).flat(true).anchor(0.5f,0.5f));
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    nearby = mMap.addMarker(new MarkerOptions().position(helperLokace).title("Nearby player").icon(BitmapDescriptorFactory.fromAsset("ping.bmp")).flat(true).anchor(0.5f,0.5f));
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                            if (elim) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        interactButton.setEnabled(false);
                                    }
                                });
                                if (nearby != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            nearby.remove();
                                            nearby = null;
                                        }
                                    });
                                }
                            }
                        }
                    } else if (divided[0].equals("startcap")) {

                    } else if (divided[0].equals("stopcap")) {

                    } else if (divided[0].equals("finishcap")) {

                    } else if (divided[0].equals("eliminate")) {
                        System.out.println("recieved eliminate");
                        if (Integer.parseInt(divided[1]) == playerID) {
                            System.out.println("dead");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    overlay.setVisibility(View.VISIBLE);
                                    View view = LayoutInflater.from(GameplayActivity.this).inflate(R.layout.overlay, null);
                                    relativeLayout.addView(view);
                                    relativeLayout.bringChildToFront(view);
                                    /*GifImageView gif = findViewById(R.id.gifelim);
                                    gif.setVisibility(View.VISIBLE);
                                    gif.*/
                                    System.out.println("really_dead");
                                }
                            });
                        }
                    }
                }
            }
        });
        recieverThread.start();
    }


    /*ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                        } else {
                            // No location access granted.
                        }
                    }
            );*/

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json);
        mMap.setMapStyle(mapStyleOptions);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                                    @Override
                                    public void onMarkerDrag(@NonNull Marker marker) {

                                    }

                                    @Override
                                    public void onMarkerDragEnd(@NonNull Marker marker) {
                                        marker.setPosition(marker.getPosition());

                                    }

                                    @Override
                                    public void onMarkerDragStart(@NonNull Marker marker) {

                                    }
                                });
                                LatLng pozice = new LatLng(location.getLatitude(),location.getLongitude());
                                CameraPosition position = new CameraPosition.Builder().
                                        target(pozice).
                                        tilt(60).
                                        zoom(19).
                                        bearing(0).
                                        build();
                                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                                hrac = mMap.addMarker(new MarkerOptions().position(pozice).title("Current position").icon(BitmapDescriptorFactory.fromAsset("player.bmp")).flat(true).anchor(0.5f,0.5f));
                                mMap.addPolygon(new PolygonOptions().strokeColor(Color.YELLOW).add(getIntent().getExtras().getParcelable("poly1"), getIntent().getExtras().getParcelable("poly2"), getIntent().getExtras().getParcelable("poly3"), getIntent().getExtras().getParcelable("poly4")));

                                if (getIntent().hasExtra("checkLoc")) {
                                    ArrayList<LatLng> tempList = getIntent().getExtras().getParcelableArrayList("checkLoc");
                                    for (LatLng latLng : tempList) {
                                        checkList.add(new Checkpoint(mMap, latLng, "time"));
                                    }

//                                    if (checkList.size() >= 1) checkList.get(0).setTime(3);
//                                    if (checkList.size() >= 2) checkList.get(1).setTime(20);
                                }
                                //Checkpoint test = new Checkpoint(mMap, getIntent().getExtras().getParcelable("poly1"), me);

//                                final Handler handler = new Handler();
//                                Runnable runnable = new Runnable() {
//                                    public void run() {
//                                        if (f) {
//                                            hrac.setIcon(BitmapDescriptorFactory.fromAsset("kamera.bmp"));
//                                            f = false;
//                                        } else {
//                                            hrac.setIcon(BitmapDescriptorFactory.fromAsset("crosshair.bmp"));
//                                            f = true;
//                                        }
//
//                                        bearing.setText(String.valueOf(mMap.getCameraPosition().tilt));
//                                        //System.out.println(String.valueOf(mMap.getCameraPosition().bearing));
//
//                                        for (Checkpoint checkpoint : checkList) {
//                                            checkpoint.inside();
//                                        }
//
//                                        handler.postDelayed(this, 1000);
//                                    }
//                                };
//                                runnable.run();
                            }
                        }
                    });
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
                    ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
        }

        LocationRequest.Builder locationRequest = new LocationRequest.Builder(1000);
        locationRequest.setMinUpdateDistanceMeters(2);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setSmallestDisplacement(1);
//        locationRequest.setInterval(2000);
//        locationRequest.setFastestInterval(1000);

        fusedLocationClient.requestLocationUpdates(locationRequest.build(),new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) return;
                else {
                    Location location = locationResult.getLastLocation();

                    if (previous == null) {
                        previous = location;
                    }

                    hrac.setRotation(previous.bearingTo(location));

                    previous = location;

                    changePositionSmoothly(hrac, new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));

                    if (teams[playerID - 1].equals("capture")) {
                        boolean cap = true;
                        for (Checkpoint checkpoint : checkList) {
                            if (checkpoint.inside(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()))) {
                                cap = false;
                                interactButton.setEnabled(true);
                            }
                        }
                        if (cap) interactButton.setEnabled(false);
                    } else if (teams[playerID - 1].equals("eliminate")) {
                        boolean elim = true;
                        for (int i = 0; i < locations.length; i++) {
                            if ((playerID - 1) != i && locations[i] != null && publicHrac.location != null && !teams[i].equals("eliminate")) {
                                float[] results = new float[1];
                                Location.distanceBetween(publicHrac.location.latitude, publicHrac.location.longitude, locations[i].latitude, locations[i].longitude, results);
                                if (results[0] <= elimDist) {
                                    eliminatable = i + 1;
                                    elim = false;
                                    interactButton.setEnabled(true);
                                    if (nearby != null) nearby.remove();
                                    nearby = mMap.addMarker(new MarkerOptions().position(locations[i]).title("Nearby player").icon(BitmapDescriptorFactory.fromAsset("ping.bmp")).flat(true).anchor(0.5f,0.5f));
                                }
                            }
                        }
                        if (elim) {
                            interactButton.setEnabled(false);
                            if (nearby != null) {
                                nearby.remove();
                                nearby = null;
                            }
                        }
                    }

                    publicHrac.location = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());

//                    String message = "Lokace: " + locationResult.getLastLocation().getLatitude() + " " + locationResult.getLastLocation().getLongitude();
//                    try {
//                        output.write(message.getBytes());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    //Location location = new Location("");
                    /*location.setLatitude(hrac.getPosition().latitude);
                    location.setLongitude(hrac.getPosition().longitude);*/

                }
            }
        }, Looper.getMainLooper());

    }

    void changePositionSmoothly(Marker marker, LatLng newLatLng) {
        if (marker == null) {
            return;
        }
        ValueAnimator animation = ValueAnimator.ofFloat(0f, 100f);
        final float[] previousStep = {0f};
        double deltaLatitude = newLatLng.latitude - marker.getPosition().latitude;
        double deltaLongitude = newLatLng.longitude - marker.getPosition().longitude;
        animation.setDuration(1000);
        animation.addUpdateListener(animation1 -> {
            float deltaStep = (Float) animation1.getAnimatedValue() - previousStep[0];
            previousStep[0] = (Float) animation1.getAnimatedValue();
            marker.setPosition(new LatLng(marker.getPosition().latitude + deltaLatitude * deltaStep * 1 / 100, marker.getPosition().longitude + deltaStep * deltaLongitude * 1 / 100));
        });
        animation.start();
    }

    @Override
    public void onClick(View view) {
        if (teams[playerID - 1].equals("capture") && publicHrac.capture) {
            boolean cap = true;
            for (int i = 0; i < checkList.size(); i++) {
                if (checkList.get(i).inside(hrac.getPosition()) && checkList.get(i).capture) {
                    checkList.get(i).capture(i);
                    cap = false;
                    changeAsync("startcap_" + i);
                    publicHrac.capture = false;
                    break;
                }
            }
            if (cap) System.out.println("No checkpoint to capture");
        } else if (teams[playerID - 1].equals("eliminate")) {
                changeAsync(("eliminate_" + eliminatable));
        }
    }

    public static class publicHrac {
        static LatLng location;
        static boolean capture = true;
    }

    public void changeAsync(String message) {
        change = message;
        sendChange = true;
    }
}