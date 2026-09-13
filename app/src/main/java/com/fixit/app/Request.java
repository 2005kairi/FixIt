package com.fixit.app;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Owner: Person 5 (Request Data Layer + Broadcast)
@Entity(tableName = "requests")
public class Request {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int tenantId;
    public String title;
    public String description;

    // TODO: Person 5 - "pending" or "done"
    public String status;

    public long createdAt;
}
