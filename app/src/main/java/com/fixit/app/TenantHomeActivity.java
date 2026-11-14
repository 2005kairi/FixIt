package com.fixit.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TenantHomeActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private TextView tvEmpty;
    private Button btnNewRequest;
    private Button btnLogout;
    private RequestAdapter requestAdapter;
    private AppDatabase database;
    private int tenantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_home);

        rvRequests = findViewById(R.id.rvRequests);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnNewRequest = findViewById(R.id.btnNewRequest);
        btnLogout = findViewById(R.id.btnLogout);

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        requestAdapter = new RequestAdapter(this, new ArrayList<>());
        rvRequests.setAdapter(requestAdapter);

        SharedPreferences preferences =
                getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);

        tenantId = preferences.getInt(LoginActivity.KEY_USER_ID, -1);

        if (tenantId == -1) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
            openLogin();
            return;
        }

        database = AppDatabase.getInstance(getApplicationContext());

        btnNewRequest.setOnClickListener(v -> {
            Intent intent = new Intent(TenantHomeActivity.this,
                    NewRequestActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            preferences.edit().clear().apply();
            openLogin();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (database != null && tenantId != -1) {
            loadRequests();
        }
    }

    private void loadRequests() {
        // Room database operations must not run on the main UI thread.
        new Thread(() -> {
            List<Request> requests = database.requestDao().getByTenant(tenantId);

            runOnUiThread(() -> {
                requestAdapter.updateRequests(requests);
                tvEmpty.setVisibility(
                        requests.isEmpty() ? View.VISIBLE : View.GONE);
                rvRequests.setVisibility(
                        requests.isEmpty() ? View.GONE : View.VISIBLE);
            });
        }).start();
    }

    private void openLogin() {
        Intent intent = new Intent(TenantHomeActivity.this,
                LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
