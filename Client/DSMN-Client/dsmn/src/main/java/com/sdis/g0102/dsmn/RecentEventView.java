package com.sdis.g0102.dsmn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;

import java.util.Random;

/**
 * Created by André on 15/05/2016.
 */
public class RecentEventView extends LinearLayout {

    private final static int EVENT_DETAIL_CODE = 4321;

    public final static String EVENT_ID = "StreetEvent.ID";
    public final static String EVENT_IS_MINE = "StreetEvent.IS_MINE";

    private ImageView icon;
    private TextView description;
    private TextView address;
    private int event_id;
    private boolean my_event;

    public RecentEventView(Context context) {
        this(context, null);
    }

    public RecentEventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.recent_event_item, this);

        icon = (ImageView) findViewById(R.id.event_icon);
        description = (TextView) findViewById(R.id.event_description);
        address = (TextView) findViewById(R.id.event_address);
    }

    public RecentEventView(Context context, AttributeSet attrs, int event_id, String description, String address, Event.Type type) {
        this(context, attrs, event_id, description, address, type, false);
    }

    public RecentEventView(Context context, AttributeSet attrs, int event_id, String description, String address, Event.Type type, boolean my_event) {
        this(context, attrs);

        this.my_event = my_event;
        this.event_id = event_id;

        if(description != null) {
            this.description.setText(description);
        }

        if(address != null) {
            this.address.setText(address);
        }

        if(type != null) {
            switch(type) {
                case RADAR:
                    this.icon.setImageResource(R.drawable.event_camera);
                    break;
                case STOP:
                    this.icon.setImageResource(R.drawable.event_stop);
                    break;
                case TRAFFIC:
                    this.icon.setImageResource(R.drawable.event_traffic);
                    break;
                case CRASH:
                    this.icon.setImageResource(R.drawable.event_crash);
                    break;
            }
        } else {
            Random r = new Random();
            switch(r.nextInt(5)) {
                case 0:
                    this.icon.setImageResource(R.drawable.event_crash);
                    break;
                case 1:
                    this.icon.setImageResource(R.drawable.event_camera);
                    break;
                case 2:
                    this.icon.setImageResource(R.drawable.event_other);
                    break;
                case 3:
                    this.icon.setImageResource(R.drawable.event_traffic);
                    break;
                case 4:
                    this.icon.setImageResource(R.drawable.event_stop);
                    break;
            }
        }

        initiateClick();
    }

    private void initiateClick() {
        final Context context = getContext();
        LinearLayout my_layout = (LinearLayout) findViewById(R.id.event_layout);
        my_layout.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra(EVENT_ID, event_id);
                intent.putExtra(EVENT_IS_MINE, my_event);
                context.startActivity(intent);
            }
        });
    }

}
