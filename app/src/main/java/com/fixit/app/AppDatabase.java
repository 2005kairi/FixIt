package com.fixit.app;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Owner: Person 6 (Database + Integration)
// Shared singleton depended on by everyone's DAOs - start once User and Request entities exist.
@Database(entities = {User.class, Request.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract RequestDao requestDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fixit_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
