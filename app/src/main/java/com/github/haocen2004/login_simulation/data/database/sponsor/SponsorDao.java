package com.github.haocen2004.login_simulation.data.database.sponsor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SponsorDao {
    @Insert
    void insertSponsors(SponsorData... sponsorDatas);

    @Update
    void updateSponsors(SponsorData... sponsorData);

    @Delete
    void deleteSponsors(SponsorData... sponsorData);

    @Query("DELETE FROM SPONSORS")
    void deleteAllSponsors();

    @Query("SELECT * FROM SPONSORS ORDER BY ID DESC")
    List<SponsorData> getAllSponsors();
}
