package com.fixit.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestDetailActivity extends AppCompatActivity {

    private TextView tvDetailTitle;
    private TextView tvDetailUnit;
    private TextView tvDetailDate;
    private TextView tvDetailDesc;
    private TextView tvDetailStatus;
    private Button btnToggleStatus;
    private Button btnContact;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Request currentRequest;
    private User currentTenant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailUnit = findViewById(R.id.tvDetailUnit);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailDesc = findViewById(R.id.tvDetailDesc);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        btnToggleStatus = findViewById(R.id.btnToggleStatus);
        btnContact = findViewById(R.id.btnContact);

        btnToggleStatus.setEnabled(false);
        btnContact.setEnabled(false);

        int requestId = getIntent().getIntExtra("request_id", -1);
        if (requestId == -1) {
            Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadRequest(requestId);

        btnToggleStatus.setOnClickListener(v -> toggleStatus());
        btnContact.setOnClickListener(v -> contactTenant());
    }

    private void loadRequest(int requestId) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            Request request = db.requestDao().getById(requestId);
            User tenant = request == null ? null : db.userDao().getById(request.tenantId);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (request == null) {
                    Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                currentRequest = request;
                currentTenant = tenant;
                displayRequest();
                btnToggleStatus.setEnabled(true);
                btnContact.setEnabled(tenant != null && tenant.email != null
                        && !tenant.email.trim().isEmpty());
            });
        });
    }

    private void displayRequest() {
        tvDetailTitle.setText(currentRequest.title == null ? "" : currentRequest.title);
        tvDetailDesc.setText(currentRequest.description == null
                ? "" : currentRequest.description);

        String unit = currentTenant == null || currentTenant.unitNumber == null
                || currentTenant.unitNumber.trim().isEmpty()
                ? "Unit: Not available"
                : "Unit: " + currentTenant.unitNumber;
        tvDetailUnit.setText(unit);

        SimpleDateFormat formatter =
                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDetailDate.setText("Submitted: "
                + formatter.format(new Date(currentRequest.createdAt)));

        boolean isDone = Request.STATUS_DONE.equals(currentRequest.status);
        tvDetailStatus.setText("Status: "
                + (isDone ? Request.STATUS_DONE : Request.STATUS_PENDING));
        tvDetailStatus.setTextColor(ContextCompat.getColor(this,
                isDone ? R.color.status_done : R.color.status_pending));

        btnToggleStatus.setText(isDone ? "Mark as Pending" : "Mark as Done");
    }

    private void toggleStatus() {
        if (currentRequest == null) return;

        btnToggleStatus.setEnabled(false);
        String newStatus = Request.STATUS_DONE.equals(currentRequest.status)
                ? Request.STATUS_PENDING : Request.STATUS_DONE;

        executor.execute(() -> {
            currentRequest.status = newStatus;
            int updatedRows = AppDatabase.getInstance(getApplicationContext())
                    .requestDao().update(currentRequest);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                btnToggleStatus.setEnabled(true);
                if (updatedRows > 0) {
                    displayRequest();
                    Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Could not update status", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void contactTenant() {
        if (currentTenant == null || currentTenant.email == null
                || currentTenant.email.trim().isEmpty()) {
            Toast.makeText(this, "Tenant email is not available",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.fromParts("mailto", currentTenant.email.trim(), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                currentRequest == null || currentRequest.title == null
                        ? "Maintenance Request"
                        : currentRequest.title);

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        } else {
            Toast.makeText(this, "No email app is available",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
