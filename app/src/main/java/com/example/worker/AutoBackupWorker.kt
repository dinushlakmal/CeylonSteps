package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.db.AppDatabase
import com.example.util.BackupTier
import com.example.util.GoogleDriveBackupEngine
import com.google.android.gms.auth.api.signin.GoogleSignIn

class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting Auto-Backup Worker...")
        val context = applicationContext

        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.w(TAG, "No Google SignIn account found. Auto-backup aborted.")
                return Result.failure()
            }

            val db = AppDatabase.getDatabase(context)
            val trips = db.tripLocationDao().getAllTripsSync()
            val journeys = db.tripTimelineDao().getAllTripsWithStopsSync()

            // Perform Backup.
            // By default, for auto backup in the background we do DATA_ONLY or DATA_WITH_IMAGES to save bandwidth.
            // A production app would read the user's preference from SharedPreferences.
            val result = GoogleDriveBackupEngine.performBackup(
                context = context,
                account = account,
                tier = BackupTier.DATA_ONLY, // Lightweight background sync
                trips = trips,
                journeys = journeys,
                onProgress = { progress, msg ->
                    Log.d(TAG, "Auto-Backup Progress [$progress%]: $msg")
                }
            )

            if (result.isSuccess) {
                Log.i(TAG, "Auto-backup successful!")
                return Result.success()
            } else {
                Log.e(TAG, "Auto-backup failed: ${result.message}")
                return Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during auto-backup", e)
            return Result.retry()
        }
    }

    companion object {
        const val TAG = "AutoBackupWorker"
        const val WORK_NAME = "AutoBackupWork"
    }
}
