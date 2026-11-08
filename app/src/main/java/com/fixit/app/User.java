package com.fixit.app;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "username", unique = true)})
public class User {

    public static final String ROLE_TENANT = "tenant";
    public static final String ROLE_LANDLORD = "landlord";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    public int userId;

    public String name;

    public String username;

    public String password;

    public String email;

    public String role;

    @ColumnInfo(name = "unit_number")
    public String unitNumber;
}
