package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.db.AppDatabase
import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import com.example.data.repository.TripRepository
import com.example.data.repository.UserManager
import com.ceylonsteps.travelapp.data.model.StopType
import com.ceylonsteps.travelapp.data.model.Trip
import com.ceylonsteps.travelapp.data.model.TripStop
import com.ceylonsteps.travelapp.data.model.TripWithStops
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupData(
    val version: Int,
    val exportedAtEpoch: Long,
    val userProfile: UserProfile?,
    val tripLocations: List<TripLocation>,
    val multiStopJourneys: List<TripWithStops>
)

data class RestoreResult(
    val isSuccess: Boolean,
    val tripsImported: Int,
    val journeysImported: Int,
    val stopsImported: Int,
    val message: String
)

object DatabaseBackupManager {

    private const val BACKUP_VERSION = 1
    private const val APP_IDENTIFIER = "CeylonSteps_TravelJournal"

    /**
     * Exports entire Room database and user profile to a formatted JSON string
     */
    fun exportToJson(
        userProfile: UserProfile,
        tripLocations: List<TripLocation>,
        multiStopJourneys: List<TripWithStops>
    ): String {
        val root = JSONObject()
        root.put("app", APP_IDENTIFIER)
        root.put("version", BACKUP_VERSION)
        root.put("exportedAtEpoch", System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        root.put("exportedAtFormatted", dateFormat.format(Date()))

        // 1. User Profile
        val profileJson = JSONObject()
        profileJson.put("userName", userProfile.userName)
        profileJson.put("profileImageUri", userProfile.profileImageUri ?: "")
        profileJson.put("homeLocationName", userProfile.homeLocationName)
        profileJson.put("homeLatitude", userProfile.homeLatitude)
        profileJson.put("homeLongitude", userProfile.homeLongitude)
        root.put("userProfile", profileJson)

        // 2. Single-Location Trips (TripLocation)
        val tripsArray = JSONArray()
        for (trip in tripLocations) {
            val tObj = JSONObject()
            tObj.put("id", trip.id)
            tObj.put("title", trip.title)
            tObj.put("description", trip.description)
            tObj.put("latitude", trip.latitude)
            tObj.put("longitude", trip.longitude)
            tObj.put("locationName", trip.locationName)
            tObj.put("dateEpochMillis", trip.dateEpochMillis)
            tObj.put("isUpcoming", trip.isUpcoming)
            tObj.put("imageUrisJson", trip.imageUrisJson)
            tObj.put("coverImageUri", trip.coverImageUri ?: "")
            tripsArray.put(tObj)
        }
        root.put("tripLocations", tripsArray)

        // 3. Multi-Stop Journeys with Itinerary Stops (TripWithStops)
        val journeysArray = JSONArray()
        for (journey in multiStopJourneys) {
            val jObj = JSONObject()
            val trip = journey.trip
            val tripJson = JSONObject()
            tripJson.put("tripId", trip.tripId)
            tripJson.put("tripTitle", trip.tripTitle)
            tripJson.put("startDateEpoch", trip.startDateEpoch)
            tripJson.put("endDateEpoch", trip.endDateEpoch ?: 0L)
            tripJson.put("originName", trip.originName)
            tripJson.put("originLatitude", trip.originLatitude)
            tripJson.put("originLongitude", trip.originLongitude)
            tripJson.put("departureTime", trip.departureTime)
            tripJson.put("totalDistanceKm", trip.totalDistanceKm)
            jObj.put("trip", tripJson)

            val stopsArray = JSONArray()
            for (stop in journey.stops.sortedBy { it.stopOrder }) {
                val sObj = JSONObject()
                sObj.put("stopId", stop.stopId)
                sObj.put("parentTripId", stop.parentTripId)
                sObj.put("stopName", stop.stopName)
                sObj.put("arrivalTime", stop.arrivalTime)
                sObj.put("departureTime", stop.departureTime ?: "")
                sObj.put("stopType", stop.stopType.name)
                sObj.put("latitude", stop.latitude)
                sObj.put("longitude", stop.longitude)
                sObj.put("notes", stop.notes)
                sObj.put("mediaUrisJson", stop.mediaUrisJson)
                sObj.put("stopOrder", stop.stopOrder)
                stopsArray.put(sObj)
            }
            jObj.put("stops", stopsArray)
            journeysArray.put(jObj)
        }
        root.put("multiStopJourneys", journeysArray)

        return root.toString(2)
    }

    /**
     * Parses a JSON backup string into structured data models
     */
    fun parseJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)
        val version = root.optInt("version", 1)
        val exportedAt = root.optLong("exportedAtEpoch", System.currentTimeMillis())

        // 1. Profile
        var profile: UserProfile? = null
        if (root.has("userProfile")) {
            val pObj = root.getJSONObject("userProfile")
            val imgUri = pObj.optString("profileImageUri").takeIf { it.isNotBlank() }
            profile = UserProfile(
                userName = pObj.optString("userName", "Traveler"),
                profileImageUri = imgUri,
                homeLocationName = pObj.optString("homeLocationName", "Colombo"),
                homeLatitude = pObj.optDouble("homeLatitude", 6.9271),
                homeLongitude = pObj.optDouble("homeLongitude", 79.8612)
            )
        }

        // 2. Single-Location Trips
        val tripLocations = mutableListOf<TripLocation>()
        if (root.has("tripLocations")) {
            val array = root.getJSONArray("tripLocations")
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val cover = obj.optString("coverImageUri").takeIf { it.isNotBlank() }
                val trip = TripLocation(
                    id = 0, // Reset for insertion/merge or will be handled in restore
                    title = obj.optString("title", "Untitled Trip"),
                    description = obj.optString("description", ""),
                    latitude = obj.optDouble("latitude", 7.8731),
                    longitude = obj.optDouble("longitude", 80.7718),
                    locationName = obj.optString("locationName", "Sri Lanka"),
                    dateEpochMillis = obj.optLong("dateEpochMillis", System.currentTimeMillis()),
                    isUpcoming = obj.optBoolean("isUpcoming", false),
                    imageUrisJson = obj.optString("imageUrisJson", "[]"),
                    coverImageUri = cover
                )
                tripLocations.add(trip)
            }
        }

        // 3. Multi-Stop Journeys
        val multiStopJourneys = mutableListOf<TripWithStops>()
        if (root.has("multiStopJourneys")) {
            val jArray = root.getJSONArray("multiStopJourneys")
            for (i in 0 until jArray.length()) {
                val jObj = jArray.getJSONObject(i)
                val tripObj = jObj.getJSONObject("trip")
                val endEpoch = tripObj.optLong("endDateEpoch", 0L).takeIf { it > 0 }

                val trip = Trip(
                    tripId = 0, // Reset ID for clean insertion
                    tripTitle = tripObj.optString("tripTitle", "Island Expedition"),
                    startDateEpoch = tripObj.optLong("startDateEpoch", System.currentTimeMillis()),
                    endDateEpoch = endEpoch,
                    originName = tripObj.optString("originName", "Home"),
                    originLatitude = tripObj.optDouble("originLatitude", 6.9271),
                    originLongitude = tripObj.optDouble("originLongitude", 79.8612),
                    departureTime = tripObj.optString("departureTime", "06:00 AM"),
                    totalDistanceKm = tripObj.optDouble("totalDistanceKm", 0.0)
                )

                val stops = mutableListOf<TripStop>()
                if (jObj.has("stops")) {
                    val sArray = jObj.getJSONArray("stops")
                    for (k in 0 until sArray.length()) {
                        val sObj = sArray.getJSONObject(k)
                        val stopTypeName = sObj.optString("stopType", "ATTRACTION")
                        val stopType = try {
                            StopType.valueOf(stopTypeName)
                        } catch (e: Exception) {
                            StopType.ATTRACTION
                        }
                        val depTime = sObj.optString("departureTime").takeIf { it.isNotBlank() }

                        val stop = TripStop(
                            stopId = 0,
                            parentTripId = 0,
                            stopName = sObj.optString("stopName", "Waypoint ${k + 1}"),
                            arrivalTime = sObj.optString("arrivalTime", "08:00 AM"),
                            departureTime = depTime,
                            stopType = stopType,
                            latitude = sObj.optDouble("latitude", 7.8731),
                            longitude = sObj.optDouble("longitude", 80.7718),
                            notes = sObj.optString("notes", ""),
                            mediaUrisJson = sObj.optString("mediaUrisJson", "[]"),
                            stopOrder = sObj.optInt("stopOrder", k + 1)
                        )
                        stops.add(stop)
                    }
                }
                multiStopJourneys.add(TripWithStops(trip = trip, stops = stops))
            }
        }

        return BackupData(
            version = version,
            exportedAtEpoch = exportedAt,
            userProfile = profile,
            tripLocations = tripLocations,
            multiStopJourneys = multiStopJourneys
        )
    }

    /**
     * Restores backup data into the local Room database and preferences
     */
    suspend fun restoreDatabase(
        context: Context,
        database: AppDatabase,
        backupData: BackupData,
        overwriteExisting: Boolean,
        restoreUserProfile: Boolean = true
    ): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val tripDao = database.tripLocationDao()
            val timelineDao = database.tripTimelineDao()

            if (overwriteExisting) {
                tripDao.deleteAllTrips()
                timelineDao.deleteAllStops()
                timelineDao.deleteAllTrips()
            }

            // 1. Insert Single-Location Trips
            var tripsImported = 0
            for (trip in backupData.tripLocations) {
                tripDao.insertTrip(trip.copy(id = 0))
                tripsImported++
            }

            // 2. Insert Multi-Stop Journeys
            var journeysImported = 0
            var stopsImported = 0
            for (journey in backupData.multiStopJourneys) {
                val newTripId = timelineDao.insertTrip(journey.trip.copy(tripId = 0))
                journeysImported++

                val newStops = journey.stops.mapIndexed { index, stop ->
                    stop.copy(
                        stopId = 0,
                        parentTripId = newTripId,
                        stopOrder = index + 1
                    )
                }
                timelineDao.insertStops(newStops)
                stopsImported += newStops.size
            }

            // 3. Restore User Profile if requested
            if (restoreUserProfile && backupData.userProfile != null) {
                val prof = backupData.userProfile
                UserManager.getInstance(context).updateProfile(
                    name = prof.userName,
                    imageUri = prof.profileImageUri,
                    homeLocationName = prof.homeLocationName,
                    homeLat = prof.homeLatitude,
                    homeLng = prof.homeLongitude
                )
            }

            RestoreResult(
                isSuccess = true,
                tripsImported = tripsImported,
                journeysImported = journeysImported,
                stopsImported = stopsImported,
                message = "Successfully imported $tripsImported single trips, $journeysImported multi-stop journeys, and $stopsImported waypoints."
            )
        } catch (e: Exception) {
            RestoreResult(
                isSuccess = false,
                tripsImported = 0,
                journeysImported = 0,
                stopsImported = 0,
                message = "Failed to import backup: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    /**
     * Writes JSON text to a SAF document Uri
     */
    fun writeJsonToUri(context: Context, uri: Uri, jsonContent: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                stream.write(jsonContent.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reads JSON text from a picked SAF file Uri
     */
    fun readJsonFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Creates a temporary JSON file and returns a Share Intent for quick backup export
     */
    fun createShareIntent(context: Context, jsonContent: String): Intent {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "CeylonSteps_Backup_$dateStr.json"
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(jsonContent.toByteArray(Charsets.UTF_8))
        }

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "CeylonSteps Travel Journal Backup ($dateStr)")
            putExtra(Intent.EXTRA_TEXT, "Here is my CeylonSteps local travel journal backup file ($fileName).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
