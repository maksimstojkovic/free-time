package io.github.silentdigit.freetime;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class Backup implements Comparable<UserTask> {

    private String taskName;
    private Location taskLocation;
    private String taskLocationString;
    private String taskAddress;
    private Integer taskDistance;
    private String taskDistanceString;
    private String taskTime;
    private TransitDataTask taskUpdate;

    //TODO: Remove the void and rename class
    public void UserTask(String name, String location, Location currentLocation, Activity activity) {
        taskName = name;
        taskLocationString = location;

        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> taskAddresses = geocoder.getFromLocationName(location, 1);

            if (taskAddresses != null && taskAddresses.size() > 0) {
                Address address = taskAddresses.get(0);
                taskAddress = address.getAddressLine(0) + "\n"; // + String.format(Locale.getDefault(), "%.3f, %.3f", address.getLatitude(), address.getLongitude());

                taskLocation = new Location("");
                taskLocation.setLatitude(address.getLatitude());
                taskLocation.setLongitude(address.getLongitude());

//                Log.i("DestInfo", destinationAddress.toString());
            } else {
                // Invalid Destination
                taskLocation = null;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        taskDistance = -1;
        taskDistanceString = "";
        taskTime = "Updating";
        updateTravelData(currentLocation, activity);
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskLocationString() {
        return taskLocationString;
    }

    public String getTaskAddress() {
        return taskAddress;
    }

    public int getTaskDistance() {
        return taskDistance;
    }

    public String getTaskDistanceString() {
        return taskDistanceString;
    }

    public String getTaskTime() {
        return taskTime;
    }

    public TransitDataTask getTaskUpdate() {
        return taskUpdate;
    }

    public void updateTravelData(Location currentLocation, Context context) {

        if (currentLocation == null || (taskUpdate != null && taskUpdate.getStatus() == AsyncTask.Status.RUNNING)) {
            return;
        }

        taskUpdate = new TransitDataTask();
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                currentLocation.getLatitude() + "," + currentLocation.getLongitude() +
                "&destination=" + taskLocation.getLatitude() + "," + taskLocation.getLongitude() +
                "&key=" + context.getString(R.string.google_maps_key) + "&mode=walking";
        taskUpdate.execute(url);
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

            String duration;

            if (taskTime == null) {
                duration = "Check API Quota";
            } else {
                duration = taskTime;
            }

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

            taskDistance = distance;
            taskTime = duration;
            setTaskDistanceString();

        }
    }


    private void setTaskDistanceString() {

//        Log.i("JSON Distance2", Integer.toString(distance));

        double estimate = (double) taskDistance;

        if (estimate < 1000) {
            estimate = Math.round(estimate/10.0) * 10;
//            dist = String.format(Locale.getDefault(), "Distance: %.0f m", estimate);
            taskDistanceString = String.format(Locale.getDefault(), "%.0f m", estimate);
        } else {
            estimate = estimate / 1000;
//            dist = String.format(Locale.getDefault(), "Distance: %.1f km", estimate);
            taskDistanceString = String.format(Locale.getDefault(), "%.1f km", estimate);
        }
    }

    @Override
    public int compareTo(@NonNull UserTask userTask) {
        Log.i("Compare", String.valueOf(Integer.compare(this.getTaskDistance(),userTask.getTaskDistance())));
        return Integer.compare(this.getTaskDistance(),userTask.getTaskDistance());
    }

}
