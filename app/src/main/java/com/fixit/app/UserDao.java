package com.fixit.app;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

// Owner: Person 2 (User Data Layer)
@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    // TODO: Person 2 - used by LoginActivity for authentication + role-based routing
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User authenticate(String email, String password);
}
