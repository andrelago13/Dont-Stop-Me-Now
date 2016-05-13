package com.sdis.g0102.dsmn.facebook;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.sdis.g0102.dsmn.R;

/**
 * Created by André on 13/05/2016.
 */
public class CustomFacebookFragment extends Fragment{

    private View view;
    private CallbackManager callbackManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.facebookfragment_layout, container, false);
        callbackManager = CallbackManager.Factory.create();

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

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d("app", "success");
            }

            @Override
            public void onCancel() {
                // App code
                Log.d("app", "cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d("app", "error");
                exception.printStackTrace();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    public static CustomFacebookFragment  newInstance(String text) {

        CustomFacebookFragment f = new CustomFacebookFragment ();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;


    }

}
