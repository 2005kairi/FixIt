package com.fixit.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// Owner: Person 4 (Landlord Side)
public class LandlordHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_home);

        // TODO: Person 4 - load all requests for this landlord's properties via RequestDao
    }
}
