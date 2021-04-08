//package com.github.haocen2004.login_simulation.Database.Announcement;
//
//import androidx.room.Dao;
//import androidx.room.Insert;
//import androidx.room.Query;
//
//import java.util.List;
//
//@Dao
//public interface AnnouncementDao {
//    @Insert
//    void insertAnnouncement(AnnouncementData... AnnouncementData);
//
//    @Query("DELETE FROM Announcement")
//    void deleteAllAnnouncements();
//
//    @Query("SELECT * FROM Announcement ORDER BY ID DESC")
//    List<AnnouncementData> getAllAnnouncements();
//}
