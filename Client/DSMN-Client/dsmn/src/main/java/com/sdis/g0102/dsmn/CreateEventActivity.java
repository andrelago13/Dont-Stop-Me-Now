package com.sdis.g0102.dsmn;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.sdis.g0102.dsmn.api.API;
import com.sdis.g0102.dsmn.api.domain.StreetEvent;

import java.io.IOException;

public class CreateEventActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String select_image_text = "Select image";
    private static final String clear_image_text = "Clear image";

    public static final int SELECT_IMAGE = 4132;
    public static final int PLACE_PICKER_REQUEST = 1112;

    private StreetEvent.Type event_type = null;
    private Place event_location = null;
    private String event_description = null;
    private Bitmap event_selected_image = null;

    private ImageView event_icon;
    private ImageView event_image;
    private Button select_picture_button;
    private LinearLayout no_location_layout;
    private LinearLayout good_location_layout;
    private TextView location_text;
    private TextView description_text;

    private API api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        api = API.getInstance();

        event_icon = (ImageView) findViewById(R.id.create_event_type_icon);
        event_image = (ImageView) findViewById(R.id.event_image);
        select_picture_button = (Button) findViewById(R.id.select_image_button);
        select_picture_button.setText(select_image_text);
        no_location_layout = (LinearLayout) findViewById(R.id.no_location_layout);
        good_location_layout = (LinearLayout) findViewById(R.id.good_location_layout);
        location_text = (TextView) findViewById(R.id.location_name);
        description_text = (TextView) findViewById(R.id.create_event_description);

        description_text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.choose_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selected = parent.getItemAtPosition(position).toString();
        Log.d("CreateEventActivity", "Selected: " + selected);

        if(selected.equals("Vehicle Crash")) {
            event_type = StreetEvent.Type.CAR_CRASH;
            event_icon.setImageResource(R.drawable.event_crash);
        } else if(selected.equals("Speed Radar")) {
            event_type = StreetEvent.Type.SPEED_RADAR;
            event_icon.setImageResource(R.drawable.event_camera);
        } else if(selected.equals("Slow Traffic")) {
            event_type = StreetEvent.Type.HIGH_TRAFFIC;
            event_icon.setImageResource(R.drawable.event_traffic);
        } else if(selected.equals("Traffic Stop")) {
            event_type = StreetEvent.Type.TRAFFIC_STOP;
            event_icon.setImageResource(R.drawable.event_stop);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d("CreateEventActivity", "Nothing selected.");
    }

    public void choosePicture(View v) {
        if(event_selected_image == null) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
        } else {
            event_selected_image = null;
            event_image.setImageDrawable(null);
            select_picture_button.setText(select_image_text);
        }
    }

    public void submit(View v) {

        event_description = description_text.getText().toString();

        if(event_description == null || event_description.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Event description cannot be empty.")
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        if(event_location == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You must select a location for the event.")
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        if(event_selected_image == null) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Submitting Event")
                    .setMessage("Are you sure you want to submit the event without a picture?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            actualSubmit();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
            return;
        }

        actualSubmit();
    }

    private void actualSubmit() {
        final Activity this_t = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Integer event_id = api.createEvent(event_type, event_description, event_location.getPlaceTypes().get(0) == Place.TYPE_SYNTHETIC_GEOCODE ? null : event_location.getName().toString(), (float)event_location.getLatLng().latitude, (float)event_location.getLatLng().longitude);
                if(event_id != null){
                    Log.d("CreateEventActivity", "Created event.");
                } else {
                    Log.d("CreateEventActivity", "Unable to create event.");
                    Toast.makeText(this_t.getApplicationContext(), "Unable to create event.", Toast.LENGTH_LONG);
                    return;
                }

                if(event_selected_image != null) {
                    if(!api.setEventPhoto(event_id, event_selected_image)) {
                        Log.d("CreateEventActivity", "Unable to upload event photo.");
                    } else {
                        Log.d("CreateEventActivity", "Event photo uploaded.");
                    }
                }

                Intent intent = new Intent(this_t, EventDetailsActivity.class);
                intent.putExtra(RecentEventView.EVENT_ID, event_id);
                intent.putExtra(RecentEventView.EVENT_IS_MINE, true);
                this_t.startActivity(intent);
            }
        }).start();
    }

    public void pickLocation(View v) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.d("CreateEventActivity", "PlacePicker exception 1");
            Toast.makeText(this, "Unable to start PlacePicker activity. Please turn on GPS and Internet connection and try again later.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.d("CreateEventActivity", "PlacePicker exception 2");
            Toast.makeText(this, "Unable to start PlacePicker activity. Please turn on GPS and Internet connection and try again later.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void clearLocation(View v) {
        event_location = null;
        good_location_layout.setVisibility(View.GONE);
        no_location_layout.setVisibility(View.VISIBLE);
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

                    event_selected_image = scaled;
                    select_picture_button.setText(clear_image_text);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this.getBaseContext(), "Unable to retrieve picture." , Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                event_location = place;
                no_location_layout.setVisibility(View.GONE);
                good_location_layout.setVisibility(View.VISIBLE);
                location_text.setText(place.getName());
            } else {
                Toast.makeText(this, "Unable to retrieve location. Please turn on GPS and Internet connection and try again.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
