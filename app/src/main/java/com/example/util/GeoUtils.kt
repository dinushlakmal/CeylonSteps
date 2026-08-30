package com.example.util

import org.osmdroid.util.GeoPoint
import kotlin.math.*

/**
 * Haversine Distance Calculator Utility
 *
 * Computes exact great-circle distance between coordinates on the Earth's surface.
 */
object GeoUtils {

    /**
     * Calculates the exact ground distance between two GeoPoints in kilometers using the Haversine formula.
     */
    fun calculateDistanceKm(start: GeoPoint, end: GeoPoint): Double {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val radiusOfEarthKm = 6371.0
        return radiusOfEarthKm * c
    }

    /**
     * Calculates the distance between latitude/longitude pairs in kilometers.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return calculateDistanceKm(GeoPoint(lat1, lon1), GeoPoint(lat2, lon2))
    }

    /**
     * Calculates the cumulative grand total distance for a list of consecutive GeoPoints in kilometers.
     */
    fun calculateTotalRouteDistance(points: List<GeoPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += calculateDistanceKm(points[i], points[i + 1])
        }
        return total
    }
}
