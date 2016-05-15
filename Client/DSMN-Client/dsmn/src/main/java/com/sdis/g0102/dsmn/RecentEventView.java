package com.sdis.g0102.dsmn;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by André on 15/05/2016.
 */
public class RecentEventView extends LinearLayout {

    public RecentEventView(Context context) {
        this(context, null);
    }

    public RecentEventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.recent_event_item, this);


        /*TextView tv = (TextView) findViewWithTag("TextView");
        if(tv != null) {
            Log.d("test", "not null");
        } else {
            Log.d("test", "null");
        }

        for(int index = 0; index < getChildCount(); ++index) {
            Log.d("test", "child" + index);
            LinearLayout nextChild = (LinearLayout) getChildAt(index);
            Log.d("test", "child" + nextChild.getChildAt(0).toString());
        }*/
    }
}
