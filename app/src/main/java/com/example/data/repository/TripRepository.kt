package com.example.data.repository

import com.example.data.dao.TripLocationDao
import com.example.data.model.SriLankaDestinations
import com.example.data.model.TripLocation
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TripRepository(private val tripDao: TripLocationDao) {

    val allTrips: Flow<List<TripLocation>> = tripDao.getAllTrips()
    val pastTrips: Flow<List<TripLocation>> = tripDao.getPastTrips()
    val upcomingTrips: Flow<List<TripLocation>> = tripDao.getUpcomingTrips()

    suspend fun getAllTripsSync(): List<TripLocation> = tripDao.getAllTripsSync()

    suspend fun getTripById(id: Long): TripLocation? = tripDao.getTripById(id)

    suspend fun insertTrip(trip: TripLocation): Long = tripDao.insertTrip(trip)

    suspend fun insertAll(trips: List<TripLocation>) = tripDao.insertAll(trips)

    suspend fun updateTrip(trip: TripLocation) = tripDao.updateTrip(trip)

    suspend fun deleteTrip(trip: TripLocation) = tripDao.deleteTrip(trip)

    suspend fun deleteTripById(id: Long) = tripDao.deleteTripById(id)

    suspend fun getTripCount(): Int = tripDao.getTripCount()

    suspend fun clearVisitedTrips() = tripDao.deleteVisitedTrips()

    suspend fun clearAllTrips() = tripDao.deleteAllTrips()

    suspend fun seedInitialDataIfEmpty() {
        // First-time users start with a clean slate; no pre-seeded visited locations.
        // Only trips added/visited by the user will be stored and displayed.
    }

    companion object {
        fun toJsonArray(uris: List<String>): String {
            val jsonArray = JSONArray()
            uris.forEach { jsonArray.put(it) }
            return jsonArray.toString()
        }

        fun parseJsonArray(jsonString: String?): List<String> {
            if (jsonString.isNullOrBlank()) return emptyList()
            return try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    val item = array.optString(i)
                    if (item.isNotBlank()) {
                        list.add(item)
                    }
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            return com.example.util.GeoUtils.calculateDistanceKm(lat1, lon1, lat2, lon2)
        }

        fun calculateTotalRouteDistance(trips: List<TripLocation>): Double {
            if (trips.size < 2) return 0.0
            val points = trips.map { org.osmdroid.util.GeoPoint(it.latitude, it.longitude) }
            return com.example.util.GeoUtils.calculateTotalRouteDistance(points)
        }

        fun getUniqueProvincesCount(trips: List<TripLocation>): Int {
            return trips.map {
                SriLankaDestinations.findMatchingProvince(it.latitude, it.longitude)
            }.distinct().size
        }
    }
}
