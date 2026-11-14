package com.fixit.app;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "requests")
public class Request {

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_DONE = "Done";

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
}
