package com.sdis.g0102.dsmn;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by André on 15/05/2016.
 */
public class RecentEventView extends LinearLayout {

    private ImageView icon;
    private TextView description;
    private TextView address;
    private int event_id;

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

    public RecentEventView(Context context, AttributeSet attrs, int event_id, String description, String address, int icon_id) {
        this(context, attrs);

        this.event_id = event_id;

        if(description != null) {
            this.description.setText(description);
        }

        if(address != null) {
            this.address.setText(address);
        }

        if(icon_id >= 0) {
            this.icon.setImageResource(icon_id);
        } else {
            Random r = new Random();
            switch(r.nextInt(4)) {
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
            }
        }

        initiateClick();
    }

    private void initiateClick() {
        LinearLayout my_layout = (LinearLayout) findViewById(R.id.event_layout);
        my_layout.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d("test", "click");
            }
        });
    }
}
