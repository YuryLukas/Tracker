package jl.narva.tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements LocListenerInterface {
    private LocationManager locationManager;
    private TrackerLocationListener locationListener;
    private Location lastLocation;
    private ProgressBar progressBar;
    //int distance; //пройденная дистанция
    Integer totalDistance=0; // планируемая дистанция
    Integer coveredDistance=0; //пройденая дистанция
    private TextView textViewVelocity, textViewDistance, textViewCoveredDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new TrackerLocationListener();
        locationListener.setLocListenerInterface(this);
        textViewVelocity = findViewById(R.id.textViewVelocity);
        textViewDistance = findViewById(R.id.textViewDistance);
        textViewCoveredDistance = findViewById(R.id.tvDistanceCovered);
                progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(1000);
        checkPermission();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, locationListener);
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title);
        ConstraintLayout cl = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_layout, null);
        builder.setView(cl);
        builder.setPositiveButton(R.string.dialog_button_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog ad = (AlertDialog) dialogInterface;
                EditText editTextNumber = ad.findViewById(R.id.editTextNumber);

                if(editTextNumber!=null && !editTextNumber.getText().equals(""))
                {
                    progressBar.setMax(Integer.parseInt(editTextNumber.getText().toString()));
                    totalDistance = Integer.parseInt(editTextNumber.getText().toString());
                    textViewDistance.setText(editTextNumber.getText().toString());
                }
                textViewCoveredDistance.setText("0");

            }
        });
        builder.show();
    }

    public TextView getTextViewCoveredDistance() {
        return textViewCoveredDistance;
    }

    public void OcClickDistanceListener(View view) {
        showDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults[0] == RESULT_OK) {
            checkPermission();
        }
        //   else {
        //      Toast.makeText(this, "No GPS permission!", Toast.LENGTH_SHORT);
        //  }
    }

    private void updateDistance(Location loc) {
        if (loc.hasSpeed() && lastLocation != null) {
            if(coveredDistance < totalDistance) coveredDistance += (int) lastLocation.distanceTo(loc);
        }
        lastLocation = loc;
        textViewVelocity.setText(String.valueOf(loc.getSpeed()));
        textViewCoveredDistance.setText(coveredDistance.toString());

    }

    @Override
    public void onChangeLocation(Location loc) {
        updateDistance(loc);
    }
}