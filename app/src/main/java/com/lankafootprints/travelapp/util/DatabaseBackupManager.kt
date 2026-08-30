package com.lankafootprints.travelapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import com.example.util.BackupData
import com.example.util.DatabaseBackupManager as BaseBackupManager
import com.example.util.RestoreResult
import com.lankafootprints.travelapp.data.model.TripWithStops

object DatabaseBackupManager {
    fun exportToJson(
        userProfile: UserProfile,
        tripLocations: List<TripLocation>,
        multiStopJourneys: List<TripWithStops>
    ): String = BaseBackupManager.exportToJson(userProfile, tripLocations, multiStopJourneys)

    fun parseJson(jsonString: String): BackupData = BaseBackupManager.parseJson(jsonString)

    suspend fun restoreDatabase(
        context: Context,
        database: AppDatabase,
        backupData: BackupData,
        overwriteExisting: Boolean,
        restoreUserProfile: Boolean = true
    ): RestoreResult = BaseBackupManager.restoreDatabase(context, database, backupData, overwriteExisting, restoreUserProfile)

    fun writeJsonToUri(context: Context, uri: Uri, jsonContent: String): Boolean =
        BaseBackupManager.writeJsonToUri(context, uri, jsonContent)

    fun readJsonFromUri(context: Context, uri: Uri): String? =
        BaseBackupManager.readJsonFromUri(context, uri)

    fun createShareIntent(context: Context, jsonContent: String): Intent =
        BaseBackupManager.createShareIntent(context, jsonContent)
}
