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
import android.os.AsyncTask;
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

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class TaskActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    Location currentLocation;
    Location destination;
    String distanceString;
    String durationString;
    TransitDataTask task;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0, locationListener);
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

                if (currentLocation != null && destination != null) {
                    updateTravelData();
                }

//                Log.i("Location: ", location.toString()); // Used for debugging
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0, locationListener);
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

        TextView destinationTextView = findViewById(R.id.destinationTextView);
        destinationTextView.setText("");

        TextView distanceTextView = findViewById(R.id.distanceTextView);
        distanceTextView.setText("");

        TextView durationTextView = findViewById(R.id.durationTextView);
        durationTextView.setText("");

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

//                Log.i("DestInfo", destinationAddress.toString());
            } else {
                destinationString = "Invalid Destination";

                TextView distanceTextView = findViewById(R.id.distanceTextView);
                distanceTextView.setText("");

                TextView durationTextView = findViewById(R.id.durationTextView);
                durationTextView.setText("");

                destination = null;

//                Log.i("DestInfo", "Invalid Destination");
            }

            TextView destTextView = findViewById(R.id.destinationTextView);
            destTextView.setText(destinationString);

            if (currentLocation != null && destination != null) {
                updateTravelData();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method used to transition to MapActivity
    public void intentMap(View view) {
        if (currentLocation != null && destination != null &&
                task != null && task.getStatus() != AsyncTask.Status.RUNNING) {
            Intent mapsIntent = new Intent(this, MapsActivity.class);

            mapsIntent.putExtra("currentLocation", currentLocation);
            mapsIntent.putExtra("destination", destination);

            mapsIntent.putExtra("distance",distanceString);
            mapsIntent.putExtra("duration",durationString);

            startActivity(mapsIntent);
        }

        if (destination == null) {
            toastMessage("Please enter a valid destination");
        }

        if (currentLocation == null) {
            toastMessage("Please wait for current location data");
        }

        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
            toastMessage("Please wait for destination data");
        }
    }

    // Method used to update distanceTextView to display distance between current location and destination
    private void updateDistance(int distance) {
        TextView distanceTextView = findViewById(R.id.distanceTextView);
        String dist;

//        Log.i("JSON Distance2", Integer.toString(distance));

        double estimate;

        if (distance < 0) {
            estimate = currentLocation.distanceTo(destination);
        } else {
            estimate = (double) distance;
        }

        if (estimate < 1000) {
            estimate = Math.round(estimate/10.0) * 10;
            dist = String.format(Locale.getDefault(), "Distance: %.0f m", estimate);
            distanceString = String.format(Locale.getDefault(), "%.0f m", estimate);
        } else {
            estimate = estimate / 1000;
            dist = String.format(Locale.getDefault(), "Distance: %.1f km", estimate);
            distanceString = String.format(Locale.getDefault(), "%.1f km", estimate);
        }

        distanceTextView.setText(dist);
    }

    // Method used to update durationTextView to display total transit time
    public void updateDuration(String duration) {
        TextView durationTextView = findViewById(R.id.durationTextView);
        durationString = duration;

        if (!duration.equals("")) {
            duration = "Transit Time: " + duration;
        }

        durationTextView.setText(duration);
    }

    public void updateTravelData() {
        task = new TaskActivity.TransitDataTask();
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                currentLocation.getLatitude() + "," + currentLocation.getLongitude() +
                "&destination=" + destination.getLatitude() + "," + destination.getLongitude() +
                "&key=" + getString(R.string.google_maps_key) + "&mode=walking";
        task.execute(url);
    }

    public class TransitDataTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result.append(current);
                    data = reader.read();
                }

                return result.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

//            Log.i("JSON", s);

            int distance = -1;
            String duration = "";

            try {
                JSONObject jsonObject = new JSONObject(s);

                JSONObject travelInfo = jsonObject.getJSONArray("routes").getJSONObject(0)
                        .getJSONArray("legs").getJSONObject(0);
                Log.i("JSON", travelInfo.toString());

                distance = Integer.parseInt(travelInfo.getJSONObject("distance").getString("value"));
                duration = travelInfo.getJSONObject("duration").getString("text");

                Log.i("JSON Distance", Integer.toString(distance));
                Log.i("JSON Duration", duration);

            }catch (Exception e) {
                e.printStackTrace();
            }

            updateDistance(distance);
            updateDuration(duration);

        }
    }

    private void toastMessage(String s) {
        Toast toast = Toast.makeText(getApplicationContext(),s, Toast.LENGTH_SHORT);
        toast.show();
    }
}
