package com.sdis.g0102.dsmn;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Looper;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.login.LoginManager;
import com.sdis.g0102.dsmn.api.API;
import com.sdis.g0102.dsmn.api.domain.Comment;
import com.sdis.g0102.dsmn.api.domain.StreetEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    public static enum ConfirmState {
        TRUE,
        FALSE,
        NONE
    }

    private int event_id;
    private boolean my_event;

    private API api;

    private TextView event_address_textview;
    private TextView event_description_textview;
    private ImageView event_confirm_true_image;
    private ImageView event_confirm_false_image;
    private ImageView event_image;
    private ImageView event_type_icon;
    private TextView event_positive_confirmations;
    private TextView event_negative_confirmations;

    private LinearLayout whole_layout;
    private LinearLayout commentsLayout;
    private RelativeLayout loading_layout;

    private int initialPositiveConfs;
    private int initialNegativeConfs;

    private ConfirmState confirmState = ConfirmState.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        loading_layout = (RelativeLayout) findViewById(R.id.loadingPanel);
        whole_layout = (LinearLayout) findViewById(R.id.whole_layout);
        event_description_textview = (TextView) findViewById(R.id.event_description);
        event_address_textview = (TextView) findViewById(R.id.event_address);
        commentsLayout = (LinearLayout) this.findViewById(R.id.comments_layout);
        event_confirm_true_image = (ImageView) findViewById(R.id.confirmButtonTrue);
        event_confirm_false_image = (ImageView) findViewById(R.id.confirmButtonFalse);
        event_type_icon = (ImageView) findViewById(R.id.event_type_icon);
        event_positive_confirmations = (TextView) findViewById(R.id.positiveConfText);
        event_negative_confirmations = (TextView) findViewById(R.id.negativeConfText);

        Bundle b = this.getIntent().getExtras();
        event_id = b.getInt(RecentEventView.EVENT_ID);
        my_event = b.getBoolean(RecentEventView.EVENT_IS_MINE);
        Log.d("EventDetailsActivity", "Accessing details for event #" + event_id + ".");

        fetchDetails();

        event_image = (ImageView) findViewById(R.id.event_image);

        initTextViewsAppearance();
        initButtons();
        initDeleteButton();
    }

    private void fetchDetails() {
        final Activity this_t = this;
        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    api = API.getInstance(this_t.getBaseContext());
                    StreetEvent event = api.getEvent(event_id);
                    detailsFetched(event);
                    List<Comment> comments = api.listEventComments(event_id);
                    initCommentsLayout(comments);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("EventDetailsActivity", "Unable to connect to DSMN server. (Exception 1)");
                    Toast.makeText(this_t.getBaseContext(),"Unable to connect to DSMN server. (Exception 1)", Toast.LENGTH_SHORT).show();
                    this_t.finish();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    Log.d("EventDetailsActivity", "Unable to connect to DSMN server. (Exception 2)");
                    Toast.makeText(this_t.getBaseContext(),"Unable to connect to DSMN server. (Exception 2)", Toast.LENGTH_SHORT).show();
                    this_t.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("EventDetailsActivity", "Unable to connect to DSMN server. (Exception 3)");
                    Toast.makeText(this_t.getBaseContext(),"Unable to connect to DSMN server. (Exception 3)", Toast.LENGTH_SHORT).show();
                    this_t.finish();
                }
            }
        }).start();
    }

    private void detailsFetched(StreetEvent event) {
        final StreetEvent event_fnl = event;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading_layout.setVisibility(View.GONE);

                event_address_textview.setText(event_fnl.location);
                event_description_textview.setText(event_fnl.description);

                if(event_fnl.creator.equals(AccessToken.getCurrentAccessToken().getUserId())) {
                    my_event = true;
                } else {
                    my_event = false;
                }

                switch(event_fnl.type) {
                    case CAR_CRASH:
                        event_type_icon.setImageResource(R.drawable.event_crash);
                        break;
                    case TRAFFIC_STOP:
                        event_type_icon.setImageResource(R.drawable.event_stop);
                        break;
                    case HIGH_TRAFFIC:
                        event_type_icon.setImageResource(R.drawable.event_traffic);
                        break;
                    case SPEED_RADAR:
                        event_type_icon.setImageResource(R.drawable.event_camera);
                        break;
                }

                initialPositiveConfs = event_fnl.positiveConfirmations;
                initialNegativeConfs = event_fnl.negativeConfirmations;
                event_positive_confirmations.setText("" + initialPositiveConfs);
                event_negative_confirmations.setText("" + initialNegativeConfs);

                initDeleteButton();
                whole_layout.invalidate();
            }
        });
        fetchPicture();
    }

    private void initDeleteButton() {
        Button delete_button = (Button) findViewById(R.id.btnDeleteEvent);
        if(!my_event) {
            delete_button.setVisibility(View.GONE);
        } else {
            delete_button.setVisibility(View.VISIBLE);
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
        final Activity this_t = this;
        new Thread( new Runnable() {
            @Override
            public void run() {
                if(api.deleteEvent(event_id)) {
                    this_t.finish();
                } else {
                    Toast.makeText(this_t, "Unable to delete event.", Toast.LENGTH_LONG);
                }
            }
        }).start();
    }

    private void initCommentsLayout(List<Comment> comments) {
        // TODO get user name
        final List<Comment> comments_fnl = comments;
        final Context ctx = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                commentsLayout.removeAllViews();
                for(Comment comment : comments_fnl) {
                    commentsLayout.addView(new EventCommentView(ctx, "" + comment.writer, comment.message, new Timestamp(comment.datetime).toString()), 0);
                }
            }
        });

    }

    private void initTextViewsAppearance() {
        event_address_textview.setMovementMethod(new ScrollingMovementMethod());

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
        final ConfirmState state_fnl = state;

        new Thread( new Runnable() {
            @Override
            public void run() {
                boolean confirmation = false;
                if(state_fnl == ConfirmState.FALSE) {
                    confirmation = api.addConfirmation(event_id, false);
                } else if (state_fnl == ConfirmState.TRUE) {
                    confirmation = api.addConfirmation(event_id, true);
                }

                final boolean confirmation_fnl = confirmation;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!confirmation_fnl) {
                            updateButtons();
                            return;
                        }

                        switch(confirmState) {
                            case NONE:
                                if(state_fnl == ConfirmState.FALSE) {
                                    event_negative_confirmations.setText("" + (initialNegativeConfs + 1));
                                } else if (state_fnl == ConfirmState.TRUE) {
                                    event_positive_confirmations.setText("" + (initialPositiveConfs + 1));
                                }
                                break;
                            case TRUE:
                            case FALSE:
                                if(state_fnl == ConfirmState.FALSE) {
                                    event_negative_confirmations.setText("" + (initialNegativeConfs + 1));
                                    event_positive_confirmations.setText("" + initialPositiveConfs);
                                } else if (state_fnl == ConfirmState.TRUE) {
                                    event_negative_confirmations.setText("" + initialNegativeConfs);
                                    event_positive_confirmations.setText("" + (initialPositiveConfs+1));
                                }
                                break;
                        }
                        confirmState = state_fnl;
                        updateButtons();
                    }
                });
            }
        }).start();
    }

    private void initButtons() {
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
        final String text = comment.getText().toString();
        if(text == null || text.equals(""))
            return;
        comment.setText("");

        final Activity this_t = this;
        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    if(api.addComment(event_id, text)) {
                        List<Comment> comments = api.listEventComments(event_id);
                        initCommentsLayout(comments);
                    } else {
                        Toast.makeText(this_t, "Unable to add comment.", Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("RecentEventsActivity", "Unable to connect to DSMN server. (Exception 4)");
                    Toast.makeText(this_t.getBaseContext(),"Unable to connect to DSMN server. (Exception 4)", Toast.LENGTH_SHORT).show();
                    this_t.finish();
                }
            }
        }).start();
    }

    private void fetchPicture() {
        final Activity this_t = this;
        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap picture = api.getEventPhoto(event_id);
                    pictureFetched(picture);
                } catch(Exception e) {
                    e.printStackTrace();
                    Log.d("EventDetailsActivity", "Unable to fetch picture (might not exist).");
                }
            }
        }).start();
    }

    private void pictureFetched(Bitmap bitmap) {
        if(bitmap == null)
            return;

        // TODO test this after adding picture to create event
        final Bitmap bitmap_fnl = bitmap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                event_image.setImageBitmap(bitmap_fnl);
            }
        });
    }
}
