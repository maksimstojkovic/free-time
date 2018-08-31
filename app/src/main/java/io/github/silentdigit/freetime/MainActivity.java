package io.github.silentdigit.freetime;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;
    private Location destination;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // If user moves more than a pre-determined threshold value, this method is called
                currentLocation = location;

                String latLong = String.format(Locale.getDefault(), "%.3f, %.3f", location.getLatitude(), location.getLongitude());
                TextView locationText = findViewById(R.id.locationTextView);
                locationText.setText(latLong);

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    if (listAddresses != null && listAddresses.size() > 0) {
                        Address currentAddress = listAddresses.get(0);
                        String addString = currentAddress.getAddressLine(0) + "\n" + locationText.getText();
                        locationText.setText(addString);

                        Log.i("PlaceInfo", currentAddress.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                updateDistance();

                Log.i("Location: ", location.toString()); // Used for debugging
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Perform fine location permissions check, else attempt to update current location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        // KeyListener for destinationEditText
        EditText destinationEditText = findViewById(R.id.destinationEditText);

        destinationEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (view.getId() == R.id.destinationEditText && keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        i == KeyEvent.KEYCODE_ENTER) {
                    setDestination(view);
                    return true;
                }
                return false;
            }
        });

        // Sets default text for location TextViews
        TextView locationText = findViewById(R.id.locationTextView);
        String searchString = "Searching...";
        locationText.setText(searchString);

        TextView destinationText = findViewById(R.id.destinationTextView);
        destinationText.setText("");

        TextView distanceText = findViewById(R.id.distanceTextView);
        distanceText.setText("");
    }

    // Method used to transition to MapActivity
    public void intentMap(View view) {
        Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);

        startActivity(mapIntent);
    }

    public void setDestination(View view) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            EditText destinationEditText = findViewById(R.id.destinationEditText);
            List<Address> destinationAddresses = geocoder.getFromLocationName(destinationEditText.getText().toString(), 1);

            String destinationString;

            if (destinationAddresses != null && destinationAddresses.size() > 0) {
                Address destinationAddress = destinationAddresses.get(0);
                destinationString = destinationAddress.getAddressLine(0) + "\n" +
                        String.format(Locale.getDefault(), "%.3f, %.3f", destinationAddress.getLatitude(),destinationAddress.getLongitude());

                destination = new Location("");
                destination.setLatitude(destinationAddress.getLatitude());
                destination.setLongitude(destinationAddress.getLongitude());

                Log.i("DestInfo", destinationAddress.toString());
            } else {
                destinationString = "Invalid Destination";

                destination = null;

                Log.i("DestInfo", "Invalid Destination");
            }

            TextView destTextView = findViewById(R.id.destinationTextView);
            destTextView.setText(destinationString);

            updateDistance();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method used to update distanceTextView to display distance between current location and destination
    private void updateDistance() {
        if (destination != null) {
            TextView distanceTextView = findViewById(R.id.distanceTextView);
            String distanceString = String.format(Locale.getDefault(), "Distance: %.0f metres", currentLocation.distanceTo(destination));
            distanceTextView.setText(distanceString);
        }

    }

    private void toastMessage() {
        Toast toast = Toast.makeText(getApplicationContext(),"It worked", Toast.LENGTH_SHORT);
        toast.show();
    }







}
