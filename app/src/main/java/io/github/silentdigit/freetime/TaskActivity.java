package io.github.silentdigit.freetime;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class TaskActivity extends AppCompatActivity {

    Location currentLocation;
    String currentLocationString;
    UserTask currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        Intent intent = getIntent();
        currentLocation = intent.getParcelableExtra("currentLocation");
        currentLocationString = intent.getStringExtra("currentLocationString");
        currentTask = intent.getParcelableExtra("userTask");


        String currentLatLong = String.format(Locale.getDefault(), "%.3f, %.3f", currentLocation.getLatitude(), currentLocation.getLongitude());
        String currentAddLatLong = currentLocationString + "\n" + currentLatLong;
        TextView locationTextView = this.findViewById(R.id.locationTextView);
        locationTextView.setText(currentAddLatLong);

        String taskName = "Task Name: " + currentTask.getTaskName();
        TextView taskNameTextView = this.findViewById(R.id.taskNameTextView);
        taskNameTextView.setText(taskName);

        String query = "Location Query: " + currentTask.getTaskLocationQuery();
        TextView queryTextView = this.findViewById(R.id.queryTextView);
        queryTextView.setText(query);

        String taskAddLatLong = currentTask.getTaskAddress() + "\n" + currentTask.getTaskLatLong();
        TextView addLongTextView = this.findViewById(R.id.addLongTextView);
        addLongTextView.setText(taskAddLatLong);

        String distanceStr = "Distance: " + currentTask.getTaskDistanceString();
        TextView distanceTextView = this.findViewById(R.id.distanceTextView);
        distanceTextView.setText(distanceStr);

        String durationStr = "Travel Time: " + currentTask.getTaskTime();
        TextView durationTextView = this.findViewById(R.id.durationTextView);
        durationTextView.setText(durationStr);

    }

    // Method used to transition to MapActivity
    public void intentMap(View view) {
        if (currentLocation != null) {
            Intent mapsIntent = new Intent(this, MapsActivity.class);

            mapsIntent.putExtra("currentLocation", currentLocation);
            mapsIntent.putExtra("currentTask", currentTask);

            mapsIntent.putExtra("distance",currentTask.getTaskDistanceString());
            mapsIntent.putExtra("duration",currentTask.getTaskTime());

            startActivity(mapsIntent);
        } else {
            toastMessage("Please wait for current location data");
        }

    }

    private void toastMessage(String s) {
        Toast toast = Toast.makeText(getApplicationContext(),s, Toast.LENGTH_SHORT);
        toast.show();
    }
}
