package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TripLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface TripLocationDao {

    @Query("SELECT * FROM trip_locations ORDER BY dateEpochMillis ASC")
    fun getAllTrips(): Flow<List<TripLocation>>

    @Query("SELECT * FROM trip_locations ORDER BY dateEpochMillis ASC")
    suspend fun getAllTripsSync(): List<TripLocation>

    @Query("SELECT * FROM trip_locations WHERE isUpcoming = 0 ORDER BY dateEpochMillis ASC")
    fun getPastTrips(): Flow<List<TripLocation>>

    @Query("SELECT * FROM trip_locations WHERE isUpcoming = 0 ORDER BY dateEpochMillis ASC")
    fun getPastTripsChronological(): Flow<List<TripLocation>>

    @Query("SELECT * FROM trip_locations WHERE isUpcoming = 1 ORDER BY dateEpochMillis ASC")
    fun getUpcomingTrips(): Flow<List<TripLocation>>

    @Query("SELECT * FROM trip_locations WHERE id = :id")
    suspend fun getTripById(id: Long): TripLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripLocation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trips: List<TripLocation>)

    @Update
    suspend fun updateTrip(trip: TripLocation)

    @Delete
    suspend fun deleteTrip(trip: TripLocation)

    @Query("DELETE FROM trip_locations WHERE id = :id")
    suspend fun deleteTripById(id: Long)

    @Query("DELETE FROM trip_locations")
    suspend fun deleteAllTrips()

    @Query("DELETE FROM trip_locations WHERE isUpcoming = 0")
    suspend fun deleteVisitedTrips()

    @Query("SELECT COUNT(*) FROM trip_locations")
    suspend fun getTripCount(): Int
}
