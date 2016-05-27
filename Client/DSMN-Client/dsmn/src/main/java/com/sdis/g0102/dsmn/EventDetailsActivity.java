package com.sdis.g0102.dsmn;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.login.LoginManager;

import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    public static enum ConfirmState {
        TRUE,
        FALSE,
        NONE
    }

    private int event_id;
    private boolean my_event;

    private TextView event_address_textview;
    private TextView event_description_textview;
    private ImageView event_confirm_true_image;
    private ImageView event_confirm_false_image;
    private ImageView event_image;

    private LinearLayout commentsLayout;

    private ConfirmState confirmState = ConfirmState.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        Bundle b = this.getIntent().getExtras();
        event_id = b.getInt(RecentEventView.EVENT_ID);
        my_event = b.getBoolean(RecentEventView.EVENT_IS_MINE);
        Log.d("EventDetailsActivity", "Accessing details for event #" + event_id + ".");

        event_image = (ImageView) findViewById(R.id.event_image);

        initTextViewsAppearance();
        initButtons();
        initCommentsLayout();
        initDeleteButton();
    }

    private void initDeleteButton() {
        Button delete_button = (Button) findViewById(R.id.btnDeleteEvent);
        if(!my_event) {
            delete_button.setVisibility(View.GONE);
        }
    }

    public void deleteEvent(View v) {
        if(!my_event)
            return;

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Deleting Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        actuallyDeleteEvent();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void actuallyDeleteEvent() {
        // TODO actually delete
    }

    private void initCommentsLayout() {
        commentsLayout = (LinearLayout) this.findViewById(R.id.comments_layout);

        for(int i = 0; i < 5; ++i) {
            commentsLayout.addView(new EventCommentView(this), i);
        }
    }

    private void initTextViewsAppearance() {
        event_address_textview = (TextView) findViewById(R.id.event_address);
        event_address_textview.setMovementMethod(new ScrollingMovementMethod());

        event_description_textview = (TextView) findViewById(R.id.event_description);
        event_description_textview.setMovementMethod(new ScrollingMovementMethod());

        event_address_textview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        event_description_textview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    private void setConfirmState(ConfirmState state) {
        // TODO send to api
        this.confirmState = state;
        updateButtons();
    }

    private void initButtons() {

        event_confirm_true_image = (ImageView) findViewById(R.id.confirmButtonTrue);
        event_confirm_false_image = (ImageView) findViewById(R.id.confirmButtonFalse);

        event_confirm_true_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConfirmState(ConfirmState.TRUE);
            }
        });

        event_confirm_false_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConfirmState(ConfirmState.FALSE);
            }
        });

        updateButtons();
    }

    private void updateButtons() {
        switch(confirmState) {
            case TRUE:
                event_confirm_true_image.setImageResource(R.mipmap.event_confirm_true_selected);
                event_confirm_false_image.setImageResource(R.mipmap.event_confirm_false);
                break;
            case FALSE:
                event_confirm_true_image.setImageResource(R.mipmap.event_confirm_true);
                event_confirm_false_image.setImageResource(R.mipmap.event_confirm_false_selected);
                break;
            case NONE:
                event_confirm_true_image.setImageResource(R.mipmap.event_confirm_true);
                event_confirm_false_image.setImageResource(R.mipmap.event_confirm_false);
                break;
        }
    }

    public void addComment(View view) {
        EditText comment = (EditText) findViewById(R.id.new_comment_edittext);
        String text = comment.getText().toString();
        comment.setText("");

        // TODO send to api
    }
}
