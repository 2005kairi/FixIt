package com.fixit.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// Owner: Person 3 (Tenant Side)
public class TenantHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_home);

        // TODO: Person 3 - load this tenant's requests via RequestDao into a RecyclerView (RequestAdapter)
    }
}
