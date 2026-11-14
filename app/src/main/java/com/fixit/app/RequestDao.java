package com.fixit.app;

import androidx.room.Dao;
import androidx.room.Delete;
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

    @Delete
    int delete(Request request);

    // Newest first (Landlord Home then moves Pending to the top)
    @Query("SELECT * FROM requests ORDER BY created_at DESC")
    List<Request> getAll();

    @Query("SELECT * FROM requests WHERE tenant_id = :tenantId ORDER BY created_at DESC")
    List<Request> getByTenant(int tenantId);

    // Only the requests from this landlord's tenants, newest first
    @Query("SELECT r.* FROM requests r INNER JOIN users u ON r.tenant_id = u.user_id "
            + "WHERE u.landlord_id = :landlordId ORDER BY r.created_at DESC")
    List<Request> getForLandlord(int landlordId);

    @Query("SELECT * FROM requests WHERE request_id = :id LIMIT 1")
    Request getById(int id);
}
