package com.sdis.g0102.dsmn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.sdis.g0102.dsmn.api.API;
import com.sdis.g0102.dsmn.api.domain.StreetEvent;
import com.sdis.g0102.dsmn.fcm.FCMActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final static int RECENT_EVENTS_CODE = 1234;

    public final static String CURRENT_TOKEN = "MainActivity.CURRENT_TOKEN";

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private AlertDialog cancelDialog;
    private AlertDialog errorDialog;

    private Button enterButton;

    public static GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .addApi(AppIndex.API).build();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();

        initEnterButton();
        initAlerts();
        initLoginButtonCallbacks();
        initTokenTracker();

        try {
            Bundle b = getIntent().getExtras();
            String event = b.getString("event");
            StreetEvent event_obj = new StreetEvent(new JSONObject(event));
            Log.d("MainActivity", "Detected event " + event_obj.id + ". Accessing details.");

            launchEventDetails(event_obj.id);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MainActivity", "No event detected. Continuing normal launch.");
        }

        detectInitialLaunch();
    }

    private void launchEventDetails(int event_id) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra(RecentEventView.EVENT_ID, event_id);
        intent.putExtra(RecentEventView.EVENT_IS_MINE, true);
        startActivity(intent);
    }

    private void detectInitialLaunch() {
        // Used to detect if login is active at startup
        if(isFacebookLoggedIn()) {
            // Already logged in with facebook
            Log.d("Facebook Login", "Login already active. Launching \"Recent Events\".");
            enterButton.setVisibility(View.VISIBLE);
            launchRecentEvents();
        } else {
            // No login active when app was started
            Log.d("Facebook Login", "No Facebook login detected. Continuing.");
            enterButton.setVisibility(View.INVISIBLE);
        }
    }

    private void initEnterButton() {
        enterButton = (Button) findViewById(R.id.enter_button);
        enterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isFacebookLoggedIn()) {
                    launchRecentEvents();
                } else {
                    cancelDialog.show();
                }
            }
        });
    }

    private void launchRecentEvents() {
        Intent intent = new Intent(this, RecentEventsActivity.class);
        intent.putExtra(CURRENT_TOKEN, AccessToken.getCurrentAccessToken().getToken());
        startActivityForResult(intent, RECENT_EVENTS_CODE);
    }

    private void initTokenTracker() {
        // Used to monitor token changing
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                Log.d("Facebook Login", "Token change");
                if(currentAccessToken == null) {
                    Log.d("Facebook Login", "Logged out");
                    enterButton.setVisibility(View.INVISIBLE);
                } else {
                    enterButton.setVisibility(View.VISIBLE);
                }
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
    }

    private void initLoginButtonCallbacks() {
        LoginButton loginButton = (LoginButton) this.findViewById(R.id.login_button_2);
        loginButton.setReadPermissions("email");

        // Callback registration, results of clicking button
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Facebook Login", "Success");
                launchRecentEvents();
                enterButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                Log.d("Facebook Login", "Cancel");
                cancelDialog.show();
                enterButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("Facebook Login", "Error");
                errorDialog.show();
                enterButton.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initAlerts() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", null);
        builder.setTitle("Warning");
        builder.setMessage("Facebook login is required to use this app.");
        cancelDialog = builder.create();
        builder.setTitle("Error");
        builder.setMessage("An error has occurred with Facebook login. Please make sure you have an active internet connection and try again later.");
        errorDialog = builder.create();
    }

    private boolean isFacebookLoggedIn() {
        AccessToken at = AccessToken.getCurrentAccessToken();
        if(at == null || at.getToken() == null) {
            // No login active
            return false;
        } else {
            // Already logged in with facebook
            return true;
        }
    }

    private void updateEnterButtonVisibility() {
        if(isFacebookLoggedIn()) {
            enterButton.setVisibility(View.VISIBLE);
        } else {
            enterButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.d("Login Activity", "Activity result triggered.");
        updateEnterButtonVisibility();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("Login Activity", "Destroyed");

        // required by Facebook
        if(accessTokenTracker != null)
            accessTokenTracker.stopTracking();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("MainActivity", "Connection Failed");
        Toast.makeText(this, "Unable to start Google API connection. Some functionalities may be unavailable.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.sdis.g0102.dsmn/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.sdis.g0102.dsmn/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
        mGoogleApiClient.disconnect();
    }
}
