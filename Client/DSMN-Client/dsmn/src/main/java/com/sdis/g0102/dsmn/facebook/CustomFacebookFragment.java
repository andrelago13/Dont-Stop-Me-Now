package com.sdis.g0102.dsmn.facebook;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.sdis.g0102.dsmn.R;

import java.util.Arrays;

/**
 * Created by André on 13/05/2016.
 */
public class CustomFacebookFragment extends Fragment{

    private View view;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.facebookfragment_layout, container, false);
        callbackManager = CallbackManager.Factory.create();

        AccessToken at = AccessToken.getCurrentAccessToken();
        if(at == null || at.getToken() == null) {
            Log.d("Test", "No login");
        } else{
            Log.d("Test", at.getToken());
        }

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization

        /*loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                callbackManager.onActivityResult(123);
            }
        });*/

        final ViewGroup container_t = container;
        final CustomFacebookFragment this_t = this;

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d("Facebook Login", "Success");
                //LoginManager.getInstance().logInWithReadPermissions(this_t, Arrays.asList("public_profile"));
                Log.d("Facebook Token", AccessToken.getCurrentAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                Log.d("Facebook Login", "Cancel");
                new AlertDialog.Builder(null)
                        .setMessage("Facebook login is required to use this application.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("Facebook Login", "Error");
                new AlertDialog.Builder(null)
                        .setMessage("An error has occurred with Facebook login. Please try again later..")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                exception.printStackTrace();
            }
        });

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

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    public static CustomFacebookFragment  newInstance(String text) {

        CustomFacebookFragment f = new CustomFacebookFragment ();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;


    }



}
