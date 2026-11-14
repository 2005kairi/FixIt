package com.fixit.app;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RequestDao {

    @Insert
    long insert(Request request);

    @Update
    int update(Request request);

    // Newest first (Landlord Home then moves Pending to the top)
    @Query("SELECT * FROM requests ORDER BY created_at DESC")
    List<Request> getAll();

    @Query("SELECT * FROM requests WHERE tenant_id = :tenantId ORDER BY created_at DESC")
    List<Request> getByTenant(int tenantId);

    @Query("SELECT * FROM requests WHERE request_id = :id LIMIT 1")
    Request getById(int id);
}
