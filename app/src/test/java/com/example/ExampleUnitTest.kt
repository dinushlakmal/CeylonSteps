package com.example

import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import com.example.util.DatabaseBackupManager
import com.lankafootprints.travelapp.data.model.StopType
import com.lankafootprints.travelapp.data.model.Trip
import com.lankafootprints.travelapp.data.model.TripStop
import com.lankafootprints.travelapp.data.model.TripWithStops
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testDatabaseBackupExportAndParse() {
        val userProfile = UserProfile(
            userName = "Dinush Lakmal",
            homeLocationName = "Kurunegala",
            homeLatitude = 7.4863,
            homeLongitude = 80.3623
        )

        val trips = listOf(
            TripLocation(
                id = 1,
                title = "Sigiriya Rock Fortress",
                description = "Ancient fortress in Central Province",
                latitude = 7.9570,
                longitude = 80.7603,
                locationName = "Sigiriya, Central Province",
                dateEpochMillis = 1700000000000L,
                isUpcoming = false,
                imageUrisJson = "[]",
                coverImageUri = null
            )
        )

        val journeys = listOf(
            TripWithStops(
                trip = Trip(
                    tripId = 10,
                    tripTitle = "Cultural Triangle Road Trip",
                    startDateEpoch = 1700000000000L,
                    endDateEpoch = 1700100000000L,
                    originName = "Home (Kurunegala)",
                    originLatitude = 7.4863,
                    originLongitude = 80.3623,
                    departureTime = "06:30 AM",
                    totalDistanceKm = 145.0
                ),
                stops = listOf(
                    TripStop(
                        stopId = 101,
                        parentTripId = 10,
                        stopName = "Dambulla Cave Temple",
                        arrivalTime = "08:15 AM",
                        departureTime = "09:30 AM",
                        stopType = StopType.ATTRACTION,
                        latitude = 7.8567,
                        longitude = 80.6483,
                        notes = "UNESCO World Heritage site",
                        mediaUrisJson = "[]",
                        stopOrder = 1
                    )
                )
            )
        )

        val json = DatabaseBackupManager.exportToJson(userProfile, trips, journeys)
        assertNotNull(json)
        assertTrue(json.contains("LankaFootprints_TravelJournal"))
        assertTrue(json.contains("Cultural Triangle Road Trip"))
        assertTrue(json.contains("Sigiriya Rock Fortress"))

        val parsed = DatabaseBackupManager.parseJson(json)
        assertEquals(1, parsed.version)
        assertEquals("Dinush Lakmal", parsed.userProfile?.userName)
        assertEquals(1, parsed.tripLocations.size)
        assertEquals("Sigiriya Rock Fortress", parsed.tripLocations[0].title)
        assertEquals(1, parsed.multiStopJourneys.size)
        assertEquals("Cultural Triangle Road Trip", parsed.multiStopJourneys[0].trip.tripTitle)
        assertEquals(1, parsed.multiStopJourneys[0].stops.size)
        assertEquals("Dambulla Cave Temple", parsed.multiStopJourneys[0].stops[0].stopName)
    }
}


