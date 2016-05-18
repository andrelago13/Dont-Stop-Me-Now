package com.sdis.g0102.dsmn;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

    private TextView event_address_textview;
    private TextView event_description_textview;
    private ImageView event_confirm_true_image;
    private ImageView event_confirm_false_image;

    private ConfirmState confirmState = ConfirmState.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        Bundle b = this.getIntent().getExtras();
        event_id = b.getInt(RecentEventView.EVENT_ID);
        Log.d("EventDetailsActivity", "Accessing details for event #" + event_id + ".");

        initTextViewsAppearance();
        initButtons();
    }

    private void initTextViewsAppearance() {
        event_address_textview = (TextView) findViewById(R.id.event_address);
        event_address_textview.setMovementMethod(new ScrollingMovementMethod());

        event_description_textview = (TextView) findViewById(R.id.event_description);
        event_description_textview.setMovementMethod(new ScrollingMovementMethod());
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
