package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_locations")
data class TripLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val dateEpochMillis: Long,
    val isUpcoming: Boolean, // False for Past, True for Upcoming
    val imageUrisJson: String, // Stored as JSON array of local URIs
    val coverImageUri: String?,
    val deletedAtEpochMillis: Long? = null
)
