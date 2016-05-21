package com.sdis.g0102.dsmn;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.IOException;

public class CreateEventActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String select_image_text = "Select image";
    private static final String clear_image_text = "Clear image";

    public static final int SELECT_IMAGE = 4132;
    public static final int PLACE_PICKER_REQUEST = 1112;

    private ImageView event_icon;
    private ImageView event_image;
    private Bitmap selected_image = null;
    private Button select_picture_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        event_icon = (ImageView) findViewById(R.id.create_event_type_icon);
        event_image = (ImageView) findViewById(R.id.event_image);
        select_picture_button = (Button) findViewById(R.id.select_image_button);
        select_picture_button.setText(select_image_text);

        Spinner spinner = (Spinner) findViewById(R.id.choose_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        //createLocationRequest();
    }

    protected void createLocationRequest() {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.d("CreateEventActivity", "PlacePicker exception 1");
            // TODO toast
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.d("CreateEventActivity", "PlacePicker exception 2");
            // TODO toast
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selected = parent.getItemAtPosition(position).toString();
        Log.d("CreateEventActivity", "Selected: " + selected);

        if(selected.equals("Vehicle Crash")) {
            event_icon.setImageResource(R.drawable.event_crash);
        } else if(selected.equals("Speed Radar")) {
            event_icon.setImageResource(R.drawable.event_camera);
        } else if(selected.equals("Slow Traffic")) {
            event_icon.setImageResource(R.drawable.event_traffic);
        } else if(selected.equals("Traffic Stop")) {
            event_icon.setImageResource(R.drawable.event_stop);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d("CreateEventActivity", "Nothing selected.");
    }

    public void choosePicture(View v) {
        if(selected_image == null) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
        } else {
            selected_image = null;
            event_image.setImageDrawable(null);
            select_picture_button.setText(select_image_text);
        }
    }

    public void submit(View v) {
        // TODO acabar
        createLocationRequest();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImageUri = data.getData();
                Log.d("CreateEventActivity", selectedImageUri.toString());

                try {
                    Bitmap original_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    int nh = (int) ( original_bitmap.getHeight() * (512.0 / original_bitmap.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(original_bitmap, 512, nh, true);
                    event_image.setImageBitmap(scaled);

                    selected_image = scaled;
                    select_picture_button.setText(clear_image_text);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this.getBaseContext(), "Unable to retrieve picture." , Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Log.d("test", "" + place.getLatLng().latitude + " " + place.getLatLng().longitude);
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            } else {
                // TODO toast
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("CreateEventActivity", "Connection Failed");
    }
}
