package com.github.haocen2004.login_simulation.data.sponsor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase

@Database(entities = [SponsorData::class], version = 1, exportSchema = false)
abstract class SponsorDatabase : RoomDatabase() {
    abstract fun sponsorDao(): SponsorDao

    companion object {
        private var INSTANCE: SponsorDatabase? = null

        fun getInstance(context: Context): SponsorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = databaseBuilder(
                    context,
                    SponsorDatabase::class.java,
                    "sponsors_db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}