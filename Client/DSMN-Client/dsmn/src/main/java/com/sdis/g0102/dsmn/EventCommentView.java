package com.sdis.g0102.dsmn;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by André on 19/05/2016.
 */
public class EventCommentView extends LinearLayout {

    private String username;
    private String content;
    private String timestamp_text;

    private TextView user_name_view;
    private TextView content_view;
    private TextView timestamp_view;

    public EventCommentView(Context context) {
        this(context, null);
    }

    public EventCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.event_comment_layout, this);

        user_name_view = (TextView) findViewById(R.id.comment_username);
        content_view = (TextView) findViewById(R.id.comment_content);
        timestamp_view = (TextView) findViewById(R.id.comment_timestamp);
    }

    public EventCommentView(Context context, String user_name, String content, String timestamp_text) {
        super(context, null);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.event_comment_layout, this);

        this.username = user_name;
        this.content = content;
        this.timestamp_text = timestamp_text;

        user_name_view = (TextView) findViewById(R.id.comment_username);
        content_view = (TextView) findViewById(R.id.comment_content);
        timestamp_view = (TextView) findViewById(R.id.comment_timestamp);

        user_name_view.setText(this.username);
        content_view.setText(this.content);
        timestamp_view.setText(this.timestamp_text);
    }

    public void setUsername(String username) {
        this.username = username;
        user_name_view.setText(this.username);
    }

    public void setContent(String content) {
        this.content = content;
        content_view.setText(this.content);
    }

    public void setTimestamp(String timestamp_text) {
        this.timestamp_text = timestamp_text;
        timestamp_view.setText(this.timestamp_text);
    }

}
