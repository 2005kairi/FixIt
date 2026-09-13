package com.fixit.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// Owner: Person 1 (Login & Signup)
// Uses User.java / UserDao.java (Person 2) for role-based routing to Tenant vs Landlord home.
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // TODO: Person 1 - wire up login form, authenticate via UserDao,
        // then route to TenantHomeActivity or LandlordHomeActivity based on role.
    }
}
