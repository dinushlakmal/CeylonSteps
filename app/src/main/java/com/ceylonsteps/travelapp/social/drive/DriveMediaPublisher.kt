package com.ceylonsteps.travelapp.social.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.Permission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections

object DriveMediaPublisher {

    private const val PUBLIC_FOLDER_NAME = "CeylonSteps_Public_Memories"

    private fun getDriveClient(context: Context, account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_FILE)
        ).apply { selectedAccount = account.account }

        return Drive.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("CeylonSteps").build()
    }

    suspend fun uploadPublicImageAndGetUrl(
        context: Context,
        account: GoogleSignInAccount,
        localFile: File
    ): String? = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveClient(context, account)
            val folderId = getOrCreatePublicFolder(drive)

            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = "ceylon_${System.currentTimeMillis()}_${localFile.name}"
                parents = listOf(folderId)
            }
            val mediaContent = FileContent("image/jpeg", localFile)
            val uploadedFile = drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()

            // Grant Public Read Permission
            val publicPermission = Permission().apply {
                type = "anyone"
                role = "reader"
            }
            drive.permissions().create(uploadedFile.id, publicPermission).execute()

            // Return direct high-speed CDN preview URL
            "https://lh3.googleusercontent.com/d/${uploadedFile.id}"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getOrCreatePublicFolder(drive: Drive): String {
        val query = drive.files().list()
            .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '$PUBLIC_FOLDER_NAME' and trashed = false")
            .setSpaces("drive")
            .execute()

        if (query.files.isNotEmpty()) {
            return query.files[0].id
        }

        val folderMeta = com.google.api.services.drive.model.File().apply {
            name = PUBLIC_FOLDER_NAME
            mimeType = "application/vnd.google-apps.folder"
        }
        val folder = drive.files().create(folderMeta).setFields("id").execute()
        return folder.id
    }
}
