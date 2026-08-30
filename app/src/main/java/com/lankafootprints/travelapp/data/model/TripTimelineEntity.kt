package com.lankafootprints.travelapp.data.model

import androidx.room.*

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val tripId: Long = 0,
    val tripTitle: String,
    val startDateEpoch: Long,
    val endDateEpoch: Long?,
    val originName: String, // e.g., "Home (Kurunegala)"
    val originLatitude: Double,
    val originLongitude: Double,
    val departureTime: String, // e.g., "06:30 AM"
    val totalDistanceKm: Double = 0.0
)

@Entity(
    tableName = "trip_stops",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["tripId"],
            childColumns = ["parentTripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentTripId")]
)
data class TripStop(
    @PrimaryKey(autoGenerate = true) val stopId: Long = 0,
    val parentTripId: Long,
    val stopName: String, // e.g., "Ambepussa Rest House"
    val arrivalTime: String, // e.g., "08:15 AM"
    val departureTime: String?, // e.g., "09:00 AM"
    val stopType: StopType, // MEAL_BREAK, FUEL, ATTRACTION, SCENIC_VIEW, HOTEL
    val latitude: Double,
    val longitude: Double,
    val notes: String,
    val mediaUrisJson: String, // JSON List of Strings storing file URIs (Images & MP4 Videos)
    val stopOrder: Int // 1, 2, 3...
)

enum class StopType(val displayName: String, val emoji: String) {
    START_POINT("Start Point", "🚩"),
    MEAL_BREAK("Meal Break", "🍽️"),
    FUEL("Fuel / Pit Stop", "⛽"),
    ATTRACTION("Attraction", "🏛️"),
    SCENIC_VIEW("Scenic View", "🌄"),
    HOTEL("Hotel / Stay", "🏨"),
    END_POINT("End Point", "🏁")
}

data class TripWithStops(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "tripId",
        entityColumn = "parentTripId"
    )
    val stops: List<TripStop>
)
