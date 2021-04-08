package com.github.haocen2004.login_simulation.Database.Sponsor;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SponsorData.class},version = 1,exportSchema = false)
public abstract class SponsorDatabase extends RoomDatabase {
    private static SponsorDatabase INSTANCE;
    static synchronized SponsorDatabase getDatabase(Context context){
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SponsorDatabase.class, "sponsors_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
    public abstract SponsorDao getSponsorDao();
}
