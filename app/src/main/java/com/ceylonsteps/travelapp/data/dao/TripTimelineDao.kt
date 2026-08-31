package com.ceylonsteps.travelapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ceylonsteps.travelapp.data.model.Trip
import com.ceylonsteps.travelapp.data.model.TripStop
import com.ceylonsteps.travelapp.data.model.TripWithStops
import kotlinx.coroutines.flow.Flow

@Dao
interface TripTimelineDao {

    @Transaction
    @Query("SELECT * FROM trips ORDER BY startDateEpoch DESC")
    fun getAllTripsWithStops(): Flow<List<TripWithStops>>

    @Transaction
    @Query("SELECT * FROM trips ORDER BY startDateEpoch ASC")
    suspend fun getAllTripsWithStopsSync(): List<TripWithStops>

    @Transaction
    @Query("SELECT * FROM trips ORDER BY startDateEpoch ASC")
    fun getAllTripsWithStopsAsc(): Flow<List<TripWithStops>>

    @Transaction
    @Query("SELECT * FROM trips WHERE tripId = :tripId")
    fun getTripWithStopsById(tripId: Long): Flow<TripWithStops?>

    @Transaction
    @Query("SELECT * FROM trips WHERE tripId = :tripId")
    suspend fun getTripWithStopsByIdSync(tripId: Long): TripWithStops?

    @Query("SELECT * FROM trips WHERE tripId = :tripId")
    suspend fun getTripById(tripId: Long): Trip?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("DELETE FROM trips WHERE tripId = :tripId")
    suspend fun deleteTripById(tripId: Long)

    @Query("SELECT * FROM trip_stops WHERE parentTripId = :tripId ORDER BY stopOrder ASC")
    fun getStopsForTrip(tripId: Long): Flow<List<TripStop>>

    @Query("SELECT * FROM trip_stops WHERE parentTripId = :tripId ORDER BY stopOrder ASC")
    suspend fun getStopsForTripSync(tripId: Long): List<TripStop>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStop(stop: TripStop): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<TripStop>)

    @Update
    suspend fun updateStop(stop: TripStop)

    @Delete
    suspend fun deleteStop(stop: TripStop)

    @Query("DELETE FROM trip_stops WHERE parentTripId = :tripId")
    suspend fun deleteStopsForTrip(tripId: Long)

    @Query("DELETE FROM trip_stops WHERE stopId = :stopId")
    suspend fun deleteStopById(stopId: Long)

    @Transaction
    suspend fun insertTripWithStops(trip: Trip, stops: List<TripStop>): Long {
        val tripId = insertTrip(trip)
        val stopsWithTripId = stops.mapIndexed { index, stop ->
            stop.copy(parentTripId = tripId, stopOrder = index + 1)
        }
        insertStops(stopsWithTripId)
        return tripId
    }

    @Transaction
    suspend fun updateTripWithStops(trip: Trip, stops: List<TripStop>) {
        updateTrip(trip)
        deleteStopsForTrip(trip.tripId)
        val stopsWithTripId = stops.mapIndexed { index, stop ->
            stop.copy(parentTripId = trip.tripId, stopOrder = index + 1)
        }
        insertStops(stopsWithTripId)
    }

    @Query("SELECT COUNT(*) FROM trips")
    suspend fun getTripCount(): Int

    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()

    @Query("DELETE FROM trip_stops")
    suspend fun deleteAllStops()
}
