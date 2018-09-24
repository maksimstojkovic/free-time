package io.github.silentdigit.freetime;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;

    Location currentLocation;
    Location destination;
    UserTask currentTask;
    String distanceString;
    String durationString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        currentLocation = intent.getParcelableExtra("currentLocation");
        currentTask = intent.getParcelableExtra("currentTask");
//        destination = intent.getParcelableExtra("destination");
        distanceString = intent.getStringExtra("distance");
        durationString = intent.getStringExtra("duration");

        if (distanceString == null) {
            distanceString = "";
        }

        if (durationString == null) {
            durationString = "";
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        TextView mapDistanceTextView = findViewById(R.id.mapDistanceTextView);
        mapDistanceTextView.setText("");

        TextView mapDurationTextView = findViewById(R.id.mapDurationTextView);
        mapDurationTextView.setText("");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add markers to map
        LatLng curr = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLng dest = new LatLng(currentTask.getTaskLocation().getLatitude(), currentTask.getTaskLocation().getLongitude());
        mMap.addMarker(new MarkerOptions().position(curr).title("Current Location"));
        mMap.addMarker(new MarkerOptions().position(dest).title("Destination"));

        TextView mapDistanceTextView = findViewById(R.id.mapDistanceTextView);
        mapDistanceTextView.setText(distanceString);

        TextView mapDurationTextView = findViewById(R.id.mapDurationTextView);
        mapDurationTextView.setText(durationString);

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        boundsBuilder.include(curr);
        boundsBuilder.include(dest);

        final LatLngBounds bounds = boundsBuilder.build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dest, 5));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150), 1000, null);

    }

    private void toastMessage() {
        Toast toast = Toast.makeText(getApplicationContext(), "It worked", Toast.LENGTH_SHORT);
        toast.show();
    }

}