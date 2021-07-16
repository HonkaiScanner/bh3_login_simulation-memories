package com.github.haocen2004.login_simulation.data.database.announcement;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AnnouncementData.class}, version = 2, exportSchema = false)
public abstract class AnnouncementDatabase extends RoomDatabase {
    private static AnnouncementDatabase INSTANCE;

    static synchronized AnnouncementDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AnnouncementDatabase.class, "announcement_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    public abstract AnnouncementDao getAnnouncementDao();
}
