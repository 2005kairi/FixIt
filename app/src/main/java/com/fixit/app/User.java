package com.fixit.app;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Owner: Person 2 (User Data Layer)
@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String email = "";

    public String password;

    // TODO: Person 2 - "tenant" or "landlord"; drives LoginActivity's routing decision
    public String role;

    public String fullName;
}
