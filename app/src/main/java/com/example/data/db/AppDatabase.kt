package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.TripLocationDao
import com.example.data.model.TripLocation
import com.ceylonsteps.travelapp.data.dao.TripTimelineDao
import com.ceylonsteps.travelapp.data.model.Trip
import com.ceylonsteps.travelapp.data.model.TripStop

@Database(entities = [Trip::class, TripStop::class, TripLocation::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tripLocationDao(): TripLocationDao
    abstract fun tripTimelineDao(): TripTimelineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lanka_footprints.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
