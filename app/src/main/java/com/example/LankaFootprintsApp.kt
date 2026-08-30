package com.example

import android.app.Application
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration
import com.example.data.db.AppDatabase
import com.example.data.repository.TripRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LankaFootprintsApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TripRepository(database.tripLocationDao()) }
    val timelineRepository by lazy { com.lankafootprints.travelapp.data.repository.TripTimelineRepository(database.tripTimelineDao()) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Initialize OSMDroid configuration for OpenStreetMap
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        Configuration.getInstance().load(this, sharedPreferences)
        Configuration.getInstance().userAgentValue = "LankaFootprints/1.0 (Android; OpenStreetMap)"
        Configuration.getInstance().osmdroidTileCache = cacheDir

        // Clean slate: ensure all old pre-seeded dummy visited records are cleared
        applicationScope.launch {
            val isCleaned = sharedPreferences.getBoolean("demo_data_purged_v1", false)
            if (!isCleaned) {
                repository.clearAllTrips()
                sharedPreferences.edit().putBoolean("demo_data_purged_v1", true).apply()
            }
        }
    }
}
