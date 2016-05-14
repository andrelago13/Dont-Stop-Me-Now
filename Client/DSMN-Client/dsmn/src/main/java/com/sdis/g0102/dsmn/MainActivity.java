package com.sdis.g0102.dsmn;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class MainActivity extends AppCompatActivity {

    public final static String CURRENT_TOKEN = "MainActivity.CURRENT_TOKEN";

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private AlertDialog cancelDialog;
    private AlertDialog errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();

        // Used to detect if login is active at startup
        if(isFacebookLoggedIn()) {
            // Already logged in with facebook
            Log.d("Facebook Login", "Login already active. Launching \"Recent Events\".");
            launchRecentEvents();
        } else {
            // No login active when app was started
            Log.d("Facebook Login", "No Facebook login detected. Continuing.");
        }

        initAlerts();
        initLoginButtonCallbacks();
        initTokenTracker();
    }

    private void launchRecentEvents() {
        Intent intent = new Intent(this, RecentEventsActivity.class);
        intent.putExtra(CURRENT_TOKEN, AccessToken.getCurrentAccessToken().getToken());
        startActivity(intent);
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
            }

            @Override
            public void onCancel() {
                Log.d("Facebook Login", "Cancel");
                cancelDialog.show();
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("Facebook Login", "Error");
                errorDialog.show();
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
        builder.setMessage("An error has occurred with Facebook login. Please try again later.");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("Test", "Destroyed");

        // required by Facebook
        if(accessTokenTracker != null)
            accessTokenTracker.stopTracking();
    }
}
