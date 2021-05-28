package com.github.haocen2004.login_simulation.data.database.announcement;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AnnouncementDao {
    @Insert
    void insertAnnouncement(AnnouncementData... announcementData);

    @Update
    void updateAnnouncement(AnnouncementData... announcementData);

    @Query("DELETE FROM ANNOUNCEMENT")
    void deleteAllAnnouncements();

    @Query("SELECT * FROM ANNOUNCEMENT ORDER BY ID DESC")
    List<AnnouncementData> getAllAnnouncements();
}
