package com.sdis.g0102.dsmn;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sdis.g0102.dsmn.api.API;
import com.sdis.g0102.dsmn.api.domain.StreetEvent;
import com.sdis.g0102.dsmn.fcm.MyFirebaseMessagingService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecentEventsActivity extends AppCompatActivity {

    public final static int CREATE_EVENT_CODE = 1324;
    public final static int MY_EVENTS_CODE = 1444;

    private AlertDialog confirmLogout;

    private RelativeLayout loading;
    private LinearLayout ll;

    private API api = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_events);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        loading = (RelativeLayout) findViewById(R.id.loadingPanel);
        setSupportActionBar(toolbar);

        /*new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 0;
                    while(i++ < 20) {
                        Log.d("test", "iter " + i);
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    Log.d("test", "left");
                }
                Log.d("test", "done");
            }
        }).start();*/

        fetchEvents();

        initCollapsingToolbarLayout();
        initFloatingActionButton();
        initDialogs();

        ll = (LinearLayout) findViewById(R.id.recent_events_linearlayout);
    }

    private void fetchEvents() {
        final Activity this_t = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    api = API.getInstance(this_t.getBaseContext());
                    List<StreetEvent> events = api.listEvents(false);
                    eventsLoaded(events);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("RecentEventsActivity", "Unable to connect to DSMN server. (Exception 1)");
                    Toast.makeText(this_t.getBaseContext(), "Unable to connect to DSMN server. (Exception 1)", Toast.LENGTH_SHORT).show();
                    this_t.finish();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    Log.d("RecentEventsActivity", "Unable to connect to DSMN server. (Exception 2)");
                    Toast.makeText(this_t.getBaseContext(), "Unable to connect to DSMN server. (Exception 2)", Toast.LENGTH_SHORT).show();
                    this_t.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("RecentEventsActivity", "Unable to connect to DSMN server. (Exception 3)");
                    Toast.makeText(this_t.getBaseContext(), "Unable to connect to DSMN server. (Exception 3)", Toast.LENGTH_SHORT).show();
                    this_t.finish();
                }
            }
        }).start();
    }

    private void initDialogs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("Logging Out");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LoginManager.getInstance().logOut();
                finish();
            }

        });
        builder.setNegativeButton("No", null);
        confirmLogout = builder.create();
    }

    private void initCollapsingToolbarLayout() {
        CollapsingToolbarLayout toolbar_layout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbar_layout.setTitle("Recent Events");

        final NestedScrollView nsv = (NestedScrollView) findViewById(R.id.scroll_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //nsv.fullScroll(ScrollView.FOCUS_UP);
                nsv.smoothScrollTo(0, 0);
            }
        });
    }

    private void initFloatingActionButton() {
        final AppCompatActivity this_t = this;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent intent = new Intent(this_t, CreateEventActivity.class);
                startActivityForResult(intent, CREATE_EVENT_CODE);
            }
        });
    }

    public void refreshList() {
        ll.removeAllViews();
        loading.setVisibility(View.VISIBLE);
        fetchEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recent_events_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.recent_events_menu_refresh:
                Log.d("RecentEvents", "\"Refresh\" pressed");
                this.refreshList();
                break;
            case R.id.recent_events_menu_myevents:
                Log.d("RecentEvents", "\"My Events\" pressed");
                this.enterMyEvents();
                break;
            case R.id.recent_events_menu_about:
                Log.d("RecentEvents", "\"About\" pressed");
                break;
            case R.id.recent_events_menu_logout:
                Log.d("RecentEvents", "\"Logout\" pressed");
                confirmLogout.show();
                break;
            case R.id.recent_events_menu_subscribe:
                Log.d("RecentEvents", "\"Subscribe\" pressed");
                changeSubscription(true);
                break;
            case R.id.recent_events_menu_unsubscribe:
                Log.d("RecentEvents", "\"Unsubscribe\" pressed");
                changeSubscription(false);
                break;
        }
        return true;
    }

    private void changeSubscription(boolean state) {
        if (state) {
            FirebaseMessaging.getInstance().subscribeToTopic("events");
            Log.d("RecentEventsActivity", "Subscribed to \"events\" topic");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Subsribed to \"events\" notifications.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            builder.create().show();
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("events");
            Log.d("RecentEventsActivity", "Unsubscribed from \"events\" topic");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Unsubsribed from \"events\" notifications.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            builder.create().show();
        }
        Log.d("test", "InstanceID token: " + FirebaseInstanceId.getInstance().getToken());
    }

    private void enterMyEvents() {
        Intent intent = new Intent(this, MyEventsActivity.class);
        startActivityForResult(intent, MY_EVENTS_CODE);
    }

    private void eventsLoaded(List<StreetEvent> events) {
        if(events == null)
            return;

        final RelativeLayout loading_final = loading;
        final Context ctx = this;
        final List<StreetEvent> list_events = events;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading_final.setVisibility(View.GONE);
                for (StreetEvent event : list_events) {
                    RecentEventView b = new RecentEventView(ctx, null, event);
                    ll.addView(b);
                }
            }
        });
    }
}
