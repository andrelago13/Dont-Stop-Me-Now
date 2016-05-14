package com.sdis.g0102.dsmn;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import com.facebook.login.LoginManager;

public class RecentEventsActivity extends AppCompatActivity {

    private AlertDialog confirmLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_events);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCollapsingToolbarLayout();
        initFloatingActionButton();
        initDialogs();
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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recent_events_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.recent_events_menu_about:
                Log.d("RecentEvents", "\"About\" pressed");
                break;
            case R.id.recent_events_menu_logout:
                Log.d("RecentEvents", "\"Logout\" pressed");
                confirmLogout.show();
                break;
        }
        return true;
    }
}
