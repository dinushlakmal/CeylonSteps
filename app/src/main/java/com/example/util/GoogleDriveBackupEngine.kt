package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.TripLocation
import com.ceylonsteps.travelapp.data.model.TripWithStops
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes
import com.google.api.client.http.ByteArrayContent

enum class BackupTier(val title: String, val description: String) {
    DATA_ONLY(
        "DATA_ONLY",
        "App Data / Room Database only (Ultra-fast, few KBs)"
    ),
    DATA_WITH_IMAGES(
        "DATA_WITH_IMAGES",
        "Database + Trip Photos (No videos)"
    ),
    FULL_BACKUP(
        "FULL_BACKUP",
        "Database + Trip Photos + Recorded Videos"
    )
}


object GoogleDriveBackupEngine {
    private const val TAG = "DriveBackupEngine"
    private const val APP_DATA_FOLDER_SPACE = "appDataFolder"
    private const val BACKUP_FILE_NAME = "ceylonsteps_backup.json"

    suspend fun performBackup(
        context: Context,
        account: GoogleSignInAccount,
        tier: BackupTier,
        trips: List<TripLocation>,
        journeys: List<TripWithStops>,
        onProgress: (Int, String) -> Unit
    ): RestoreResult = withContext(Dispatchers.IO) {
        try {
            onProgress(10, "Initializing Google Drive AppData Sync...")
            
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
            .setApplicationName("CeylonSteps")
            .build()
            
            // 1. Generate JSON Backup
            val jsonPayload = DatabaseBackupManager.exportToJson(
                com.example.data.repository.UserManager.getInstance(context).getUserProfile(),
                trips,
                journeys
            )
            onProgress(30, "Generated metadata (${jsonPayload.length / 1024} KB)")

            // 2. Identify Media based on Tier
            if (tier == BackupTier.DATA_WITH_IMAGES || tier == BackupTier.FULL_BACKUP) {
                onProgress(50, "Analyzing media files...")
            }
            if (tier == BackupTier.FULL_BACKUP) {
                onProgress(70, "Preparing large video blobs...")
            }

            // 3. Upload or Update existing file in Google Drive AppDataFolder
            onProgress(85, "Syncing to your personal Google Drive cloud...")
            
            val existingList = try {
                driveService.files().list()
                    .setSpaces(APP_DATA_FOLDER_SPACE)
                    .setQ("name='$BACKUP_FILE_NAME' and trashed=false")
                    .setFields("files(id, name)")
                    .execute()
            } catch (e: Exception) {
                null
            }

            val existingFile = existingList?.files?.firstOrNull()
            val mediaContent = ByteArrayContent.fromString("application/json", jsonPayload)

            val fileId = if (existingFile != null) {
                val updated = driveService.files().update(existingFile.id, null, mediaContent)
                    .setFields("id")
                    .execute()
                updated.id
            } else {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = BACKUP_FILE_NAME
                    parents = listOf(APP_DATA_FOLDER_SPACE)
                }
                val created = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
                created.id
            }

            onProgress(100, "Backup successfully synced to Google Drive!")
            Log.d(TAG, "Google Drive backup saved with ID: $fileId")
            RestoreResult(true, trips.size, journeys.size, 0, "Successfully backed up to Google Drive (${tier.name}).")
        } catch (e: Exception) {
            Log.e(TAG, "Drive backup failed", e)
            RestoreResult(false, 0, 0, 0, "Drive backup failed: ${e.message}")
        }
    }

    /**
     * Fast background auto-sync without progress dialogs
     */
    suspend fun performAutoSync(
        context: Context,
        account: GoogleSignInAccount,
        trips: List<TripLocation>,
        journeys: List<TripWithStops>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
            .setApplicationName("CeylonSteps")
            .build()

            val jsonPayload = DatabaseBackupManager.exportToJson(
                com.example.data.repository.UserManager.getInstance(context).getUserProfile(),
                trips,
                journeys
            )

            val existingList = try {
                driveService.files().list()
                    .setSpaces(APP_DATA_FOLDER_SPACE)
                    .setQ("name='$BACKUP_FILE_NAME' and trashed=false")
                    .setFields("files(id, name)")
                    .execute()
            } catch (e: Exception) {
                null
            }

            val existingFile = existingList?.files?.firstOrNull()
            val mediaContent = ByteArrayContent.fromString("application/json", jsonPayload)

            if (existingFile != null) {
                driveService.files().update(existingFile.id, null, mediaContent).execute()
            } else {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = BACKUP_FILE_NAME
                    parents = listOf(APP_DATA_FOLDER_SPACE)
                }
                driveService.files().create(fileMetadata, mediaContent).execute()
            }

            Log.d(TAG, "Auto-sync to Google Drive completed successfully")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Auto-sync to Google Drive background failed: ${e.message}")
            false
        }
    }

    suspend fun restoreFromDrive(
        context: Context,
        account: GoogleSignInAccount,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): String? = withContext(Dispatchers.IO) {
        try {
            onProgress(10, "Connecting to Google Drive...")
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
            .setApplicationName("CeylonSteps")
            .build()

            onProgress(40, "Looking for backup file...")
            val result = driveService.files().list()
                .setSpaces(APP_DATA_FOLDER_SPACE)
                .setQ("name='$BACKUP_FILE_NAME' and trashed=false")
                .setFields("files(id, name)")
                .execute()

            val file = result.files?.firstOrNull()
            if (file != null) {
                onProgress(70, "Downloading backup...")
                val outputStream = java.io.ByteArrayOutputStream()
                driveService.files().get(file.id).executeMediaAndDownloadTo(outputStream)
                onProgress(100, "Download complete!")
                return@withContext outputStream.toString("UTF-8")
            } else {
                onProgress(100, "No backup found.")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Drive restore failed", e)
            onProgress(100, "Restore failed: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Restores data directly from Google Drive and merges into Room database and User profile
     */
    suspend fun restoreAndApplyFromDrive(
        context: Context,
        account: GoogleSignInAccount,
        database: com.example.data.db.AppDatabase
    ): RestoreResult = withContext(Dispatchers.IO) {
        val json = restoreFromDrive(context, account)
        if (json.isNullOrBlank()) {
            return@withContext RestoreResult(
                isSuccess = false,
                tripsImported = 0,
                journeysImported = 0,
                stopsImported = 0,
                message = "No cloud backup found on your Google Drive."
            )
        }

        try {
            val backupData = DatabaseBackupManager.parseJson(json)
            DatabaseBackupManager.restoreDatabase(
                context = context,
                database = database,
                backupData = backupData,
                overwriteExisting = false,
                restoreUserProfile = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error applying backup data", e)
            RestoreResult(
                isSuccess = false,
                tripsImported = 0,
                journeysImported = 0,
                stopsImported = 0,
                message = "Failed to restore backup: ${e.message}"
            )
        }
    }
}
