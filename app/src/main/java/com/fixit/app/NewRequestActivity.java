package com.fixit.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// Owner: Person 3 (Tenant Side)
public class NewRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);

        // TODO: Person 3 - build the form, save via RequestDao, then send the
        // "com.fixit.app.NEW_REQUEST" broadcast that NewRequestReceiver listens for.
    }
}
