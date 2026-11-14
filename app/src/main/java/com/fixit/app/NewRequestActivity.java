package com.fixit.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewRequestActivity extends AppCompatActivity {

    private TextView tvFormTitle;
    private EditText etTitle;
    private EditText etDescription;
    private Button btnSubmit;
    private Button btnDelete;

    // -1 means "create a new request"; otherwise the id of the request being edited.
    private int editRequestId = -1;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvFormTitle = findViewById(R.id.tvFormTitle);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnDelete = findViewById(R.id.btnDelete);

        btnSubmit.setOnClickListener(view -> submitRequest());
        btnDelete.setOnClickListener(view -> confirmDelete());

        editRequestId = getIntent().getIntExtra("request_id", -1);
        if (editRequestId != -1) {
            loadRequestForEdit();
        }
    }

    private int currentTenantId() {
        SharedPreferences preferences = getSharedPreferences(
                LoginActivity.PREFS_NAME, MODE_PRIVATE);
        return preferences.getInt(LoginActivity.KEY_USER_ID, -1);
    }

    private void loadRequestForEdit() {
        int tenantId = currentTenantId();
        databaseExecutor.execute(() -> {
            Request request = AppDatabase.getInstance(getApplicationContext())
                    .requestDao().getById(editRequestId);

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (request == null || request.tenantId != tenantId) {
                    Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (!Request.STATUS_PENDING.equals(request.status)) {
                    Toast.makeText(this, "Completed requests can't be edited",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                tvFormTitle.setText("Edit Request");
                etTitle.setText(request.title);
                etDescription.setText(request.description);
                btnSubmit.setText("Save Changes");
                btnDelete.setVisibility(android.view.View.VISIBLE);
            });
        });
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

        if (editRequestId != -1) {
            saveChanges(title, description);
            return;
        }

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

    private void saveChanges(String title, String description) {
        databaseExecutor.execute(() -> {
            RequestDao dao = AppDatabase.getInstance(getApplicationContext()).requestDao();
            // Re-read so a status change made meanwhile is never overwritten.
            Request request = dao.getById(editRequestId);

            String error = null;
            if (request == null) {
                error = "Request no longer exists";
            } else if (!Request.STATUS_PENDING.equals(request.status)) {
                error = "This request can no longer be edited";
            } else {
                request.title = title;
                request.description = description;
                dao.update(request);
            }

            String message = error;
            mainHandler.post(() -> {
                if (message == null) {
                    Toast.makeText(this, "Request updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete request?")
                .setMessage("This can't be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteRequest())
                .show();
    }

    private void deleteRequest() {
        btnDelete.setEnabled(false);
        databaseExecutor.execute(() -> {
            RequestDao dao = AppDatabase.getInstance(getApplicationContext()).requestDao();
            Request request = dao.getById(editRequestId);

            String message;
            if (request == null) {
                message = "Request no longer exists";
            } else if (!Request.STATUS_PENDING.equals(request.status)) {
                message = "This request can no longer be deleted";
            } else {
                dao.delete(request);
                message = "Request deleted";
            }

            mainHandler.post(() -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}
