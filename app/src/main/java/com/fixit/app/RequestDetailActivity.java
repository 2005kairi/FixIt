package com.fixit.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private Button btnReject;
    private Button btnContact;
    private TextView btnTenantInfo;
    private View tenantInfoPanel;
    private TextView tvTenantName;
    private TextView tvTenantUnit;
    private TextView tvTenantEmail;
    private View rejectReasonCard;
    private TextView tvRejectReasons;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Request currentRequest;
    private User currentTenant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailUnit = findViewById(R.id.tvDetailUnit);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailDesc = findViewById(R.id.tvDetailDesc);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        btnToggleStatus = findViewById(R.id.btnToggleStatus);
        btnReject = findViewById(R.id.btnReject);
        btnContact = findViewById(R.id.btnContact);
        btnTenantInfo = findViewById(R.id.btnTenantInfo);
        tenantInfoPanel = findViewById(R.id.tenantInfoPanel);
        tvTenantName = findViewById(R.id.tvTenantName);
        tvTenantUnit = findViewById(R.id.tvTenantUnit);
        tvTenantEmail = findViewById(R.id.tvTenantEmail);
        rejectReasonCard = findViewById(R.id.rejectReasonCard);
        tvRejectReasons = findViewById(R.id.tvRejectReasons);

        btnTenantInfo.setOnClickListener(v -> {
            boolean show = tenantInfoPanel.getVisibility() != View.VISIBLE;
            tenantInfoPanel.setVisibility(show ? View.VISIBLE : View.GONE);
            btnTenantInfo.setText(show ? "−" : "+");
        });

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
        btnReject.setOnClickListener(v -> showRejectDialog());
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

        boolean noName = currentTenant == null || currentTenant.name == null
                || currentTenant.name.trim().isEmpty();
        boolean noEmail = currentTenant == null || currentTenant.email == null
                || currentTenant.email.trim().isEmpty();
        tvTenantName.setText("Name: " + (noName ? "Not available" : currentTenant.name.trim()));
        tvTenantUnit.setText(unit);
        tvTenantEmail.setText("Email: " + (noEmail ? "Not available" : currentTenant.email.trim()));

        SimpleDateFormat formatter =
                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDetailDate.setText("Submitted: "
                + formatter.format(new Date(currentRequest.createdAt)));

        boolean isDone = Request.STATUS_DONE.equals(currentRequest.status);
        boolean isRejected = Request.STATUS_REJECTED.equals(currentRequest.status);
        boolean isPending = !isDone && !isRejected;

        String statusLabel = isDone ? Request.STATUS_DONE
                : isRejected ? Request.STATUS_REJECTED : Request.STATUS_PENDING;
        tvDetailStatus.setText("Status: " + statusLabel);
        tvDetailStatus.setTextColor(ContextCompat.getColor(this,
                isDone ? R.color.status_done
                        : isRejected ? R.color.fixit_danger : R.color.status_pending));

        btnToggleStatus.setText(isDone ? "Mark as Pending"
                : isRejected ? "Reopen Request" : "Mark as Done");
        btnReject.setVisibility(isPending ? View.VISIBLE : View.GONE);

        rejectReasonCard.setVisibility(isRejected ? View.VISIBLE : View.GONE);
        if (isRejected) {
            tvRejectReasons.setText(formatReasons(currentRequest.rejectReasons));
        }
    }

    // "A|B" -> "• A\n• B"
    static String formatReasons(String stored) {
        if (stored == null || stored.trim().isEmpty()) return "No reason given";
        StringBuilder text = new StringBuilder();
        for (String reason : stored.split("\\" + Request.REASON_SEPARATOR)) {
            if (text.length() > 0) text.append("\n");
            text.append("• ").append(reason);
        }
        return text.toString();
    }

    private void toggleStatus() {
        if (currentRequest == null) return;

        btnToggleStatus.setEnabled(false);
        // Pending -> Done; Done or Rejected -> Pending (rejection reasons are cleared)
        boolean wasPending = !Request.STATUS_DONE.equals(currentRequest.status)
                && !Request.STATUS_REJECTED.equals(currentRequest.status);
        String newStatus = wasPending ? Request.STATUS_DONE : Request.STATUS_PENDING;

        executor.execute(() -> {
            currentRequest.status = newStatus;
            if (!Request.STATUS_REJECTED.equals(newStatus)) {
                currentRequest.rejectReasons = null;
            }
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

    private void showRejectDialog() {
        if (currentRequest == null) return;

        boolean[] checked = new boolean[Request.REJECT_REASONS.length];
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Why reject this request?")
                .setMultiChoiceItems(Request.REJECT_REASONS, checked,
                        (d, which, isChecked) -> checked[which] = isChecked)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reject", null)
                .create();
        dialog.show();

        // Set here so the dialog stays open when nothing is ticked.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            List<String> chosen = new ArrayList<>();
            for (int i = 0; i < checked.length; i++) {
                if (checked[i]) chosen.add(Request.REJECT_REASONS[i]);
            }
            if (chosen.isEmpty()) {
                Toast.makeText(this, "Select at least one reason",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            rejectRequest(TextUtils.join(Request.REASON_SEPARATOR, chosen));
        });
    }

    private void rejectRequest(String reasons) {
        btnReject.setEnabled(false);
        executor.execute(() -> {
            RequestDao dao = AppDatabase.getInstance(getApplicationContext()).requestDao();
            // Re-read so a change made meanwhile is never overwritten.
            Request fresh = dao.getById(currentRequest.id);

            String error = null;
            if (fresh == null) {
                error = "Request no longer exists";
            } else if (Request.STATUS_DONE.equals(fresh.status)
                    || Request.STATUS_REJECTED.equals(fresh.status)) {
                error = "This request can no longer be rejected";
            } else {
                fresh.status = Request.STATUS_REJECTED;
                fresh.rejectReasons = reasons;
                dao.update(fresh);
                currentRequest = fresh;
            }

            String message = error;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                btnReject.setEnabled(true);
                if (message == null) {
                    displayRequest();
                    Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
