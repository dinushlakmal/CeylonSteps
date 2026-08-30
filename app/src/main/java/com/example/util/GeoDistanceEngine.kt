package com.example.util

import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import org.osmdroid.util.GeoPoint
import java.util.Locale
import java.util.regex.Pattern

/**
 * Distance From Home Calculation & Geospatial Parsing Engine
 */
object GeoDistanceEngine {

    // Regex to match and extract "latitude, longitude" pairs from text or URLs
    private val COORDINATE_REGEX = Pattern.compile(
        "[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)"
    )

    /**
     * Calculates direct distance between a trip coordinate and the user's home base in kilometers.
     */
    fun calculateDistanceFromHomeKm(
        tripLat: Double,
        tripLon: Double,
        homeLat: Double,
        homeLon: Double
    ): Double {
        return GeoUtils.calculateDistanceKm(
            GeoPoint(tripLat, tripLon),
            GeoPoint(homeLat, homeLon)
        )
    }

    /**
     * Calculates direct distance between a TripLocation and the UserProfile home base.
     */
    fun calculateDistanceFromHomeKm(trip: TripLocation, profile: UserProfile): Double {
        return calculateDistanceFromHomeKm(
            trip.latitude,
            trip.longitude,
            profile.homeLatitude,
            profile.homeLongitude
        )
    }

    /**
     * Calculates the total round-trip exploration distance:
     * Home -> First Stop -> Stop 2 -> ... -> Last Stop -> Home
     */
    fun calculateRoundTripDistanceKm(trips: List<TripLocation>, profile: UserProfile): Double {
        if (trips.isEmpty()) return 0.0

        val homePoint = GeoPoint(profile.homeLatitude, profile.homeLongitude)
        val sorted = trips.sortedBy { it.dateEpochMillis }

        var total = 0.0
        // From home to first stop
        total += GeoUtils.calculateDistanceKm(homePoint, GeoPoint(sorted.first().latitude, sorted.first().longitude))

        // Intermediate stops
        for (i in 0 until sorted.size - 1) {
            total += GeoUtils.calculateDistanceKm(
                GeoPoint(sorted[i].latitude, sorted[i].longitude),
                GeoPoint(sorted[i + 1].latitude, sorted[i + 1].longitude)
            )
        }

        // Return trip from last stop back to home
        total += GeoUtils.calculateDistanceKm(GeoPoint(sorted.last().latitude, sorted.last().longitude), homePoint)

        return total
    }

    /**
     * Extracts latitude and longitude from Google Maps URLs, OpenStreetMap URLs, or raw text inputs.
     * Supports formats like:
     * - "6.9271, 79.8612"
     * - "https://www.google.com/maps/@6.9271,79.8612,15z"
     * - "https://maps.google.com/?q=6.9271,79.8612"
     * - "https://www.openstreetmap.org/#map=16/6.9271/79.8612"
     */
    fun extractCoordinatesFromText(input: String): Pair<Double, Double>? {
        if (input.isBlank()) return null

        // 1. Try OSM format (e.g. #map=zoom/lat/lon)
        val osmPattern = Pattern.compile("#map=\\d+/([-+]?\\d+\\.\\d+)/([-+]?\\d+\\.\\d+)")
        val osmMatcher = osmPattern.matcher(input)
        if (osmMatcher.find()) {
            val lat = osmMatcher.group(1)?.toDoubleOrNull()
            val lon = osmMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lon != null) {
                return Pair(lat, lon)
            }
        }

        // 2. Try standard coordinate regex (e.g. "6.9271, 79.8612" or within Google Maps URLs)
        val matcher = COORDINATE_REGEX.matcher(input)
        if (matcher.find()) {
            val matchedStr = matcher.group(0)
            val parts = matchedStr.split(",")
            if (parts.size == 2) {
                val lat = parts[0].trim().toDoubleOrNull()
                val lon = parts[1].trim().toDoubleOrNull()
                if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
                    return Pair(lat, lon)
                }
            }
        }

        return null
    }

    /**
     * Formats distance nicely as string (e.g. "184 km from Home Base")
     */
    fun formatDistanceFromHome(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            "${String.format(Locale.US, "%.0f", distanceKm * 1000)} m from Home Base"
        } else {
            "${String.format(Locale.US, "%,.0f", distanceKm)} km from Home Base"
        }
    }
}
