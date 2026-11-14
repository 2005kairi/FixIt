package com.fixit.app;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "requests")
public class Request {

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_DONE = "Done";
    public static final String STATUS_REJECTED = "Rejected";

    // Checklist the landlord picks from when rejecting a request.
    public static final String[] REJECT_REASONS = {
            "Not covered by the lease",
            "Damage caused by the tenant",
            "Duplicate request",
            "Needs more information",
            "Already scheduled or fixed",
            "Other"
    };
    // Chosen reasons are stored in one column, joined with this separator.
    public static final String REASON_SEPARATOR = "|";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "request_id")
    public int id;

    @ColumnInfo(name = "tenant_id")
    public int tenantId;

    public String title;
    public String description;
    public String status;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    // Set only while status is Rejected.
    @ColumnInfo(name = "reject_reasons")
    public String rejectReasons;
}
