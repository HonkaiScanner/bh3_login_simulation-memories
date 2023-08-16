package com.github.haocen2004.login_simulation.data.sponsor.database

import androidx.room.*

@Dao
interface SponsorDao {
    @Insert
    fun insertSponsors(vararg sponsorDatas: SponsorData?)

    @Update
    fun updateSponsors(vararg sponsorData: SponsorData?)

    @Delete
    fun deleteSponsors(vararg sponsorData: SponsorData?)

    @Query("DELETE FROM SPONSORS")
    fun deleteAllSponsors()

    @get:Query("SELECT * FROM SPONSORS ORDER BY ID DESC")
    val allSponsors: List<SponsorData>

    @Query("SELECT * FROM SPONSORS ORDER BY ID DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedList(limit: Int?, offset: Int?): List<SponsorData>
}