package com.github.haocen2004.login_simulation.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SponsorDao {
    @Insert
    void insertSponsors(SponsorData... sponsorDatas);

    @Query("DELETE FROM SPONSORS")
    void deleteAllSponsors();

    @Query("SELECT * FROM SPONSORS ORDER BY ID DESC")
    List<SponsorData> getAllSponsors();
}
