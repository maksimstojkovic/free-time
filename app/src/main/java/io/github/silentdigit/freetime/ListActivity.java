package io.github.silentdigit.freetime;

import android.Manifest;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

public class ListActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    Location currentLocation;

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
        setContentView(R.layout.activity_list);

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
//                TODO: Check if this is usable for updating transit times, might need to write another method to update time and distance for all tasks
//                if (currentLocation != null && destination != null) {
//                    updateTravelData();
//                }

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

        Comparator<UserTask> taskComparator = new Comparator<UserTask>() {
            @Override
            public int compare(UserTask t1, UserTask t2) {
                if (t1.getTime() < t2.getTime()) {
                    return -1;
                } else if (t1.getTime() > t2.getTime()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };

        PriorityQueue <UserTask> taskQueue = new PriorityQueue<>(taskComparator);

//        String[] sampleTaskArray = {"",""};
//        ArrayList<String> sampleTaskList = new ArrayList<>(Arrays.asList(sampleTaskArray));


//        try {
//            List<Address> taskAddresses = geocoder.getFromLocationName("", 1);
//            String taskString;
//
//            if (taskAddresses != null && taskAddresses.size() > 0) {
//                Address taskAddress = taskAddresses.get(0);
//                taskString = taskAddress.getAddressLine(0) + "\n" +
//                        String.format(Locale.getDefault(), "%.3f, %.3f", taskAddress.getLatitude(),taskAddress.getLongitude());
//
//                destination = new Location("");
//                destination.setLatitude(destinationAddress.getLatitude());
//                destination.setLongitude(destinationAddress.getLongitude());
//
////                Log.i("DestInfo", destinationAddress.toString());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // Updates taskListView to display list of active tasks in taskQueue
        ListView taskListView = findViewById(R.id.taskListView);
        ArrayList<UserTask> taskList = new ArrayList<>(Arrays.asList(taskQueue.toArray(new UserTask[0])));
        ArrayAdapter<UserTask> taskAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,taskList);

        taskListView.setAdapter(taskAdapter);

    }


}
