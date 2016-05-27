package com.sdis.g0102.dsmn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.sdis.g0102.dsmn.api.domain.StreetEvent;

public class MyEventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        LinearLayout ll = (LinearLayout) findViewById(R.id.main_layout);

        for(int i = 0; i < 20; ++i) {
            RecentEventView b = new RecentEventView(this, null, i, "desc", "addr", StreetEvent.Type.CAR_CRASH, true);

            ll.addView(b);
        }
    }
}
