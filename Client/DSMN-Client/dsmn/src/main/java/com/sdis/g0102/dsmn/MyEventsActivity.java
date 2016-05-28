package com.sdis.g0102.dsmn;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sdis.g0102.dsmn.api.API;
import com.sdis.g0102.dsmn.api.domain.StreetEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class MyEventsActivity extends AppCompatActivity {

    private API api;

    private LinearLayout ll;
    private RelativeLayout loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        api = API.getInstance();

        ll = (LinearLayout) findViewById(R.id.main_layout);
        loading = (RelativeLayout) findViewById(R.id.loadingPanel);

        loadEvents();
    }

    private void loadEvents() {
        final Activity this_t = this;
        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    api = API.getInstance(this_t.getBaseContext());
                    List<StreetEvent> events = api.listEvents(true);
                    eventsLoaded(events);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("MyEventsActivity", "Unable to connect to DSMN server. (Exception 1)");
                    Toast.makeText(this_t.getApplicationContext(),"Unable to connect to DSMN server. (Exception 1)", Toast.LENGTH_SHORT).show();
                    this_t.finish();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    Log.d("MyEventsActivity", "Unable to connect to DSMN server. (Exception 2)");
                    Toast.makeText(this_t.getApplicationContext(),"Unable to connect to DSMN server. (Exception 2)", Toast.LENGTH_SHORT).show();
                    this_t.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("MyEventsActivity", "Unable to connect to DSMN server. (Exception 3)");
                    Toast.makeText(this_t.getApplicationContext(),"Unable to connect to DSMN server. (Exception 3)", Toast.LENGTH_SHORT).show();
                    this_t.finish();
                }
            }
        }).start();
    }

    private void eventsLoaded(List<StreetEvent> events) {
        final RelativeLayout loading_final = loading;
        final Context ctx = this;
        final List<StreetEvent> list_events = events;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading_final.setVisibility(View.GONE);
                for(StreetEvent event : list_events) {
                    RecentEventView b = new RecentEventView(ctx, null, event);
                    ll.addView(b);
                }
            }
        });
    }
}
