package ga.denis.outplay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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

import ga.denis.outplay.databinding.ActivityGameplayBinding;
import ga.denis.outplay.databinding.ActivityMapsBinding;

public class GameplayActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityGameplayBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker me;
    private boolean f = false;
    TextView bearing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGameplayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bearing = (TextView) findViewById(R.id.textView);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager()
                .findFragmentById(R.id.gameplay);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
                                LatLng pozice = new LatLng(location.getLatitude(),location.getLongitude());
                                CameraPosition position = new CameraPosition.Builder().
                                        target(pozice).
                                        tilt(60).
                                        zoom(19).
                                        bearing(0).
                                        build();
                                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                                me = mMap.addMarker(new MarkerOptions().position(pozice).title("Current position").icon(BitmapDescriptorFactory.fromAsset("kamera.bmp")).flat(true).anchor(0.5f,0.5f));
                                mMap.addPolygon(new PolygonOptions().strokeColor(Color.YELLOW).add(getIntent().getExtras().getParcelable("poly1"), getIntent().getExtras().getParcelable("poly2"), getIntent().getExtras().getParcelable("poly4"), getIntent().getExtras().getParcelable("poly3")));



                                final Handler handler = new Handler();
                                Runnable runnable = new Runnable() {
                                    public void run() {
                                        if (f) {
                                            me.setIcon(BitmapDescriptorFactory.fromAsset("kamera.bmp"));
                                            f = false;
                                        } else {
                                            me.setIcon(BitmapDescriptorFactory.fromAsset("crosshair.bmp"));
                                            f = true;
                                        }

                                        bearing.setText(String.valueOf(mMap.getCameraPosition().tilt));
                                        //System.out.println(String.valueOf(mMap.getCameraPosition().bearing));

                                        handler.postDelayed(this, 1000);
                                    }
                                };
                                runnable.run();
                            }
                        }
                    });

        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
                    ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        fusedLocationClient.requestLocationUpdates(new LocationRequest(),new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) return;
                else {
                    me.setPosition(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));
                }
            }
        }, Looper.getMainLooper());
    }
}