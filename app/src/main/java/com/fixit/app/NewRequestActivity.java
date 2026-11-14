package com.fixit.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewRequestActivity extends AppCompatActivity {

    private EditText etTitle;
    private EditText etDescription;
    private Button btnSubmit;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(view -> submitRequest());
    }

    private void submitRequest() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences(
                LoginActivity.PREFS_NAME,
                MODE_PRIVATE
        );

        int tenantId = preferences.getInt(LoginActivity.KEY_USER_ID, -1);
        String role = preferences.getString(LoginActivity.KEY_ROLE, "");

        if (tenantId == -1 || !User.ROLE_TENANT.equals(role)) {
            Toast.makeText(
                    this,
                    "Please log in with a tenant account",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnSubmit.setEnabled(false);

        databaseExecutor.execute(() -> {
            try {
                Request request = new Request();
                request.tenantId = tenantId;
                request.title = title;
                request.description = description;
                request.status = Request.STATUS_PENDING;
                request.createdAt = System.currentTimeMillis();

                AppDatabase.getInstance(getApplicationContext())
                        .requestDao()
                        .insert(request);

                mainHandler.post(() -> {
                    Intent broadcast = new Intent(
                            NewRequestReceiver.ACTION_NEW_REQUEST
                    );
                    broadcast.setPackage(getPackageName());
                    sendBroadcast(broadcast);

                    Toast.makeText(
                            NewRequestActivity.this,
                            "Request submitted",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });

            } catch (Exception exception) {
                mainHandler.post(() -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(
                            NewRequestActivity.this,
                            "Unable to submit request. Please try again.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}
