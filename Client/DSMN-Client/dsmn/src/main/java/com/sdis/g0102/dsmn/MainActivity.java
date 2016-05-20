package com.sdis.g0102.dsmn;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.sdis.g0102.dsmn.api.API;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class MainActivity extends AppCompatActivity {

    private final static int RECENT_EVENTS_CODE = 1234;

    public final static String CURRENT_TOKEN = "MainActivity.CURRENT_TOKEN";

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private AlertDialog cancelDialog;
    private AlertDialog errorDialog;

    private Button enterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();
        new Thread() {
            @Override
            public void run() {
                try {
                    new API(getApplicationContext(), new URL("https://192.168.1.69/api/")).listEvents();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        initEnterButton();
        initAlerts();
        initLoginButtonCallbacks();
        initTokenTracker();

        detectInitialLaunch();
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

}
