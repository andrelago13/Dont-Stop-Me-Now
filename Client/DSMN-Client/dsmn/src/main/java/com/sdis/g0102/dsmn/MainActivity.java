package com.sdis.g0102.dsmn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
    }
}
