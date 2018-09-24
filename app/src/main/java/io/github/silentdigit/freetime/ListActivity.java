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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

public class ListActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    Location currentLocation;
    String currentLocationString;
    ArrayList<UserTask> taskQueue;
    boolean startup;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        startup = true;

        TextView locationText = findViewById(R.id.locationListTextView);
        String searchString = "Searching...";
        locationText.setText(searchString);

        UserTask sampleTaskOne = new UserTask("Group Meeting", "State Library Sydney", currentLocation,this);
        UserTask sampleTaskTwo = new UserTask("Buy Uni Supplies", "Camperdown Officeworks", currentLocation,this);
        UserTask sampleTaskThree = new UserTask("Buy Lunch", "Subway Usyd", currentLocation,this);
        UserTask sampleTaskFour = new UserTask("ENGG2062 Lecture", "Merewether Usyd", currentLocation,this);
        UserTask sampleTaskFive = new UserTask("Basketball Game", "Marrickville PCYC", currentLocation,this);

        taskQueue = new ArrayList<>();

        taskQueue.add(sampleTaskOne);
        taskQueue.add(sampleTaskTwo);
        taskQueue.add(sampleTaskThree);
        taskQueue.add(sampleTaskFour);
        taskQueue.add(sampleTaskFive);

        updateListView();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // If user moves more than a pre-determined threshold value, this method is called
                currentLocation = location;

                String latLong = String.format(Locale.getDefault(), "%.3f, %.3f", location.getLatitude(), location.getLongitude());
                TextView locationText = findViewById(R.id.locationListTextView);
                locationText.setText(latLong);

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    if (listAddresses != null && listAddresses.size() > 0) {
                        Address currentAddress = listAddresses.get(0);
                        currentLocationString = currentAddress.getAddressLine(0);
                        String addString = currentLocationString + "\n" + locationText.getText();
                        locationText.setText(addString);

                        Log.i("PlaceInfo", currentAddress.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (taskQueue != null && taskQueue.size() > 0) {


                    for (UserTask task:taskQueue) {
                        task.updateTravelData(currentLocation, getApplicationContext());
                    }

                    if (startup) {
                        startup = false;
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateListView();
                            }
                        }, 2000);
                    } else {
                        updateListView();
                    }
                }
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
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0, locationListener);
        }

        ListView taskListView = findViewById(R.id.taskListView);

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLocation != null) {
                    Intent taskIntent = new Intent(getApplicationContext(), TaskActivity.class);

                    taskIntent.putExtra("currentLocation", currentLocation);
                    taskIntent.putExtra("currentLocationString", currentLocationString);
                    Log.i("Loc String", currentLocationString);
                    taskIntent.putExtra("userTask", taskQueue.get(i));

                    startActivity(taskIntent);
                } else {
                    toastMessage("Please wait for current location data");
                }
            }
        });

    }

    private void updateListView() {
        // Updates taskListView to display list of active tasks in taskQueue
        ListView taskListView = findViewById(R.id.taskListView);
        UserTask[] taskArray = taskQueue.toArray(new UserTask[0]);
        Arrays.sort(taskArray);
        taskQueue = new ArrayList<>(Arrays.asList(taskArray));
        ArrayAdapter<UserTask> taskAdapter = new ArrayAdapter<UserTask>(this,android.R.layout.simple_list_item_2,android.R.id.text1,taskQueue) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                String nameLoc = taskQueue.get(position).getTaskName(); // + " - " + taskList.get(position).getTaskLocationString()

                text1.setText(nameLoc);

                String addTimeDist = taskQueue.get(position).getTaskAddress() + "\n" +
                        taskQueue.get(position).getTaskTime();

                if (!taskQueue.get(position).getTaskDistanceString().equals("")) {
                    addTimeDist += " - " + taskQueue.get(position).getTaskDistanceString();
                }

                text2.setLineSpacing(5f,1f);
                text2.setText(addTimeDist);

                return view;
            }
        };

        taskListView.setAdapter(taskAdapter);
    }


    private void toastMessage(String s) {
        Toast toast = Toast.makeText(getApplicationContext(),s, Toast.LENGTH_SHORT);
        toast.show();
    }

}
