package com.fixit.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// Owner: Person 4 (Landlord Side)
public class RequestDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        // TODO: Person 4 - show request details, let the landlord mark it done via RequestDao
    }
}
