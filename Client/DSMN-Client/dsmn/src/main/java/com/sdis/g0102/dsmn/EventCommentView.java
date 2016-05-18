package com.sdis.g0102.dsmn;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * Created by André on 19/05/2016.
 */
public class EventCommentView extends LinearLayout {

    public EventCommentView(Context context) {
        this(context, null);
    }

    public EventCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.event_comment_layout, this);
    }

}
