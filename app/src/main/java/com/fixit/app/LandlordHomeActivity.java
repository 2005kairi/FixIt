package com.fixit.app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LandlordHomeActivity extends AppCompatActivity {
    private RecyclerView rvAllRequests;
    private TextView tvEmpty;
    private final List<RowData> rows = new ArrayList<>();
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private LandlordAdapter adapter;
    private boolean receiverRegistered = false;

    private final BroadcastReceiver refreshReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null &&
                    NewRequestReceiver.ACTION_NEW_REQUEST_REFRESH
                            .equals(intent.getAction())) {
                loadRequests();
            }
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_landlord_home);
        rvAllRequests = findViewById(R.id.rvAllRequests);
        tvEmpty = findViewById(R.id.tvEmpty);
        Button btnLogout = findViewById(R.id.btnLogout);

        rvAllRequests.setLayoutManager(
                new LinearLayoutManager(this));
        adapter = new LandlordAdapter();
        rvAllRequests.setAdapter(adapter);
        btnLogout.setOnClickListener(v -> logout());
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(
                NewRequestReceiver.ACTION_NEW_REQUEST_REFRESH);
        ContextCompat.registerReceiver(this, refreshReceiver,
                filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        receiverRegistered = true;
    }

    @Override
    protected void onStop() {
        if (receiverRegistered) {
            unregisterReceiver(refreshReceiver);
            receiverRegistered = false;
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }

    private void loadRequests() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(
                    getApplicationContext());
            List<Request> all = db.requestDao().getAll();
            all.sort((a, b) -> Boolean.compare(
                    "pending".equalsIgnoreCase(b.status),
                    "pending".equalsIgnoreCase(a.status)));
            List<RowData> updated = new ArrayList<>();
            for (Request r : all) {
                User tenant = db.userDao().getById(r.tenantId);
                String unit = tenant != null &&
                        tenant.unitNumber != null
                        ? tenant.unitNumber : "Unknown";
                updated.add(new RowData(r, unit));
            }
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                rows.clear();
                rows.addAll(updated);
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(rows.isEmpty()
                        ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void logout() {
        getSharedPreferences(LoginActivity.PREFS_NAME,
                MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private static class RowData {
        final Request request;
        final String unit;
        RowData(Request request, String unit) {
            this.request = request;
            this.unit = unit;
        }
    }

    // Landlord-only rows avoid changing Person 3's adapter.
    private class LandlordAdapter extends
            RecyclerView.Adapter<LandlordAdapter.Holder> {
        class Holder extends RecyclerView.ViewHolder {
            TextView title, description, date, status, unit;

            Holder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvTitle);
                description = itemView.findViewById(R.id.tvDesc);
                date = itemView.findViewById(R.id.tvDate);
                status = itemView.findViewById(R.id.tvStatus);
                unit = itemView.findViewById(R.id.tvUnit);
            }
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_request, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder h, int position) {
            RowData data = rows.get(position);
            Request r = data.request;
            h.title.setText(r.title);
            h.description.setText(r.description);
            h.date.setText(DateFormat.getDateInstance()
                    .format(new Date(r.createdAt)));
            h.unit.setVisibility(View.VISIBLE);
            h.unit.setText("Unit: " + data.unit);
            boolean pending = "pending".equalsIgnoreCase(r.status);
            h.status.setText(pending ? "Pending" : "Done");
            h.status.setTextColor(ContextCompat.getColor(
                    LandlordHomeActivity.this,
                    pending ? R.color.status_pending : R.color.status_done));
            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(
                        LandlordHomeActivity.this,
                        RequestDetailActivity.class);
                intent.putExtra("request_id", r.id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }
    }
}
