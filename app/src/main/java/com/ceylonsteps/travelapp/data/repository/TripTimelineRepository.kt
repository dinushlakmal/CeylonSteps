package com.ceylonsteps.travelapp.data.repository

import com.example.util.GeoUtils
import com.ceylonsteps.travelapp.data.dao.TripTimelineDao
import com.ceylonsteps.travelapp.data.model.StopType
import com.ceylonsteps.travelapp.data.model.Trip
import com.ceylonsteps.travelapp.data.model.TripStop
import com.ceylonsteps.travelapp.data.model.TripWithStops
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

class TripTimelineRepository(private val timelineDao: TripTimelineDao) {

    val allTripsWithStops: Flow<List<TripWithStops>> = timelineDao.getAllTripsWithStops()

    fun getTripWithStopsById(tripId: Long): Flow<TripWithStops?> =
        timelineDao.getTripWithStopsById(tripId)

    suspend fun getTripWithStopsByIdSync(tripId: Long): TripWithStops? =
        timelineDao.getTripWithStopsByIdSync(tripId)

    suspend fun saveMultiStopTrip(trip: Trip, stops: List<TripStop>): Long {
        val totalDistance = calculateTotalJourneyDistance(
            trip.originLatitude,
            trip.originLongitude,
            stops
        )
        val tripWithDistance = trip.copy(totalDistanceKm = totalDistance)

        return if (trip.tripId == 0L) {
            timelineDao.insertTripWithStops(tripWithDistance, stops)
        } else {
            timelineDao.updateTripWithStops(tripWithDistance, stops)
            trip.tripId
        }
    }

    suspend fun getAllTripsWithStopsSync(): List<TripWithStops> =
        timelineDao.getAllTripsWithStopsSync()

    suspend fun clearAllJourneys() {
        timelineDao.deleteAllStops()
        timelineDao.deleteAllTrips()
    }

    suspend fun insertTripWithStopsDirect(trip: Trip, stops: List<TripStop>): Long {
        return timelineDao.insertTripWithStops(trip, stops)
    }

    suspend fun deleteTrip(tripId: Long) {
        timelineDao.deleteTripById(tripId)
    }

    suspend fun deleteStop(stopId: Long) {
        timelineDao.deleteStopById(stopId)
    }

    companion object {
        fun calculateTotalJourneyDistance(
            originLat: Double,
            originLon: Double,
            stops: List<TripStop>
        ): Double {
            if (stops.isEmpty()) return 0.0

            var totalKm = 0.0
            var currentLat = originLat
            var currentLon = originLon

            val sortedStops = stops.sortedBy { it.stopOrder }
            for (stop in sortedStops) {
                totalKm += GeoUtils.calculateDistanceKm(
                    currentLat,
                    currentLon,
                    stop.latitude,
                    stop.longitude
                )
                currentLat = stop.latitude
                currentLon = stop.longitude
            }
            return (totalKm * 10).toInt() / 10.0 // Round to 1 decimal
        }

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
    }
}
