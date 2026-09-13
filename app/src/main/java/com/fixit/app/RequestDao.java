package com.fixit.app;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// Owner: Person 5 (Request Data Layer + Broadcast)
@Dao
public interface RequestDao {

    @Insert
    long insert(Request request);

    @Update
    void update(Request request);

    @Query("SELECT * FROM requests WHERE tenantId = :tenantId")
    List<Request> getForTenant(int tenantId);

    @Query("SELECT * FROM requests")
    List<Request> getAll();
}
