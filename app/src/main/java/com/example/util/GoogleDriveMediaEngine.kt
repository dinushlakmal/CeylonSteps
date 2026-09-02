package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

data class MediaUploadResult(
    val success: Boolean,
    val directCdnUrls: List<String> = emptyList(),
    val fileIds: List<String> = emptyList(),
    val errorMessage: String? = null
)

object GoogleDriveMediaEngine {
    private const val TAG = "DriveMediaEngine"
    private const val PUBLIC_MEDIA_FOLDER_NAME = "CeylonSteps_Public_Media"

    /**
     * Uploads a single image (e.g. Cover Photo, Avatar) to Google Drive and returns
     * its direct Google CDN URL (e.g. https://lh3.googleusercontent.com/d/{FILE_ID}).
     */
    suspend fun uploadSingleImage(
        context: Context,
        account: GoogleSignInAccount,
        imageUri: String,
        filePrefix: String = "ceylon_cover"
    ): String? = withContext(Dispatchers.IO) {
        if (imageUri.isBlank() || imageUri.startsWith("http://") || imageUri.startsWith("https://")) {
            return@withContext imageUri.takeIf { it.isNotBlank() }
        }

        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
            )
            credential.selectedAccount = account.account

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("CeylonSteps")
                .build()

            val folderId = getOrCreatePublicMediaFolder(driveService)
            val uri = Uri.parse(imageUri)
            val (fileBytes, mimeType) = readBytesAndMimeType(context, uri)

            if (fileBytes == null || fileBytes.isEmpty()) {
                Log.w(TAG, "Failed to read bytes for single image upload: $imageUri")
                return@withContext null
            }

            val timestamp = System.currentTimeMillis()
            val extension = if (mimeType.contains("png")) "png" else if (mimeType.contains("webp")) "webp" else "jpg"
            val fileName = "${filePrefix}_${timestamp}.$extension"

            val fileMetadata = File().apply {
                name = fileName
                parents = listOf(folderId)
                this.mimeType = mimeType
            }

            val mediaContent = ByteArrayContent(mimeType, fileBytes)
            val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name, mimeType")
                .execute()

            val fileId = uploadedFile.id

            // Grant public read access
            try {
                val publicPermission = Permission().apply {
                    type = "anyone"
                    role = "reader"
                }
                driveService.permissions().create(fileId, publicPermission).execute()
            } catch (pe: Exception) {
                Log.w(TAG, "Failed setting permissions for single image $fileId: ${pe.message}")
            }

            val directCdnUrl = "https://lh3.googleusercontent.com/d/$fileId"
            Log.d(TAG, "Single image uploaded to Google Drive CDN: $directCdnUrl")
            directCdnUrl
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload single image to Drive", e)
            null
        }
    }

    /**
     * Uploads media files (photos/videos) directly to a public Google Drive folder
     * under the user's account ($0 media hosting cost).
     * Sets public reader permissions and generates direct CDN image links:
     * https://lh3.googleusercontent.com/d/{FILE_ID}
     */
    suspend fun uploadMediaList(
        context: Context,
        account: GoogleSignInAccount,
        mediaUris: List<String>,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): MediaUploadResult = withContext(Dispatchers.IO) {
        if (mediaUris.isEmpty()) {
            return@withContext MediaUploadResult(success = true, directCdnUrls = emptyList())
        }

        try {
            onProgress(5, "Connecting to Google Drive media storage...")
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
            )
            credential.selectedAccount = account.account

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("CeylonSteps")
                .build()

            onProgress(15, "Locating public community media folder...")
            val folderId = getOrCreatePublicMediaFolder(driveService)

            val uploadedCdnUrls = mutableListOf<String>()
            val uploadedFileIds = mutableListOf<String>()

            val total = mediaUris.size
            for ((index, uriString) in mediaUris.withIndex()) {
                val currentNum = index + 1
                val baseProgress = 20 + ((currentNum - 1) * 70 / total)
                onProgress(baseProgress, "Uploading photo $currentNum of $total to Google Drive...")

                val uri = Uri.parse(uriString)
                val (fileBytes, mimeType) = readBytesAndMimeType(context, uri)

                if (fileBytes == null || fileBytes.isEmpty()) {
                    Log.w(TAG, "Failed to read media bytes from uri: $uriString")
                    // If it's already an HTTP URL (e.g. sample or pre-uploaded), preserve it
                    if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                        uploadedCdnUrls.add(uriString)
                    }
                    continue
                }

                val timestamp = System.currentTimeMillis()
                val extension = if (mimeType.contains("video")) "mp4" else "jpg"
                val fileName = "ceylon_post_media_${timestamp}_$currentNum.$extension"

                val fileMetadata = File().apply {
                    name = fileName
                    parents = listOf(folderId)
                    this.mimeType = mimeType
                }

                val mediaContent = ByteArrayContent(mimeType, fileBytes)

                val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, mimeType, webContentLink")
                    .execute()

                val fileId = uploadedFile.id
                uploadedFileIds.add(fileId)

                onProgress(
                    baseProgress + (35 / total),
                    "Generating \$0 direct CDN streaming link ($currentNum/$total)..."
                )

                // Make the file publicly readable by 'anyone' with role 'reader'
                try {
                    val publicPermission = Permission().apply {
                        type = "anyone"
                        role = "reader"
                    }
                    driveService.permissions().create(fileId, publicPermission).execute()
                } catch (pe: Exception) {
                    Log.w(TAG, "Permission setting warning for file $fileId: ${pe.message}")
                }

                // Canonical direct Google user content image link
                val directCdnUrl = "https://lh3.googleusercontent.com/d/$fileId"
                uploadedCdnUrls.add(directCdnUrl)
                Log.d(TAG, "Media successfully hosted on Drive CDN: $directCdnUrl")
            }

            onProgress(100, "All media successfully hosted on Google Drive!")
            MediaUploadResult(
                success = true,
                directCdnUrls = uploadedCdnUrls,
                fileIds = uploadedFileIds
            )
        } catch (e: Exception) {
            Log.e(TAG, "Drive media upload failed", e)
            MediaUploadResult(
                success = false,
                errorMessage = "Google Drive media upload error: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    private fun getOrCreatePublicMediaFolder(driveService: Drive): String {
        try {
            // Check if folder already exists in root drive
            val query = "name = '$PUBLIC_MEDIA_FOLDER_NAME' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
            val fileList = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            val existingFolder = fileList.files?.firstOrNull()
            if (existingFolder != null) {
                return existingFolder.id
            }

            // Create new public folder
            val folderMetadata = File().apply {
                name = PUBLIC_MEDIA_FOLDER_NAME
                mimeType = "application/vnd.google-apps.folder"
            }

            val createdFolder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute()

            // Grant public read permission to the folder
            try {
                val folderPerm = Permission().apply {
                    type = "anyone"
                    role = "reader"
                }
                driveService.permissions().create(createdFolder.id, folderPerm).execute()
            } catch (e: Exception) {
                Log.w(TAG, "Failed setting folder permission", e)
            }

            return createdFolder.id
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving public media folder", e)
            return "root"
        }
    }

    private fun readBytesAndMimeType(context: Context, uri: Uri): Pair<ByteArray?, String> {
        val scheme = uri.scheme
        val mimeType = context.contentResolver.getType(uri) ?: when {
            uri.toString().endsWith(".mp4", ignoreCase = true) -> "video/mp4"
            uri.toString().endsWith(".png", ignoreCase = true) -> "image/png"
            uri.toString().endsWith(".webp", ignoreCase = true) -> "image/webp"
            else -> "image/jpeg"
        }

        return try {
            if (scheme == "content" || scheme == "file") {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() }
                Pair(bytes, mimeType)
            } else if (scheme == "http" || scheme == "https") {
                // Pre-existing remote URL
                Pair(null, mimeType)
            } else {
                val file = java.io.File(uri.path ?: "")
                if (file.exists()) {
                    Pair(file.readBytes(), mimeType)
                } else {
                    Pair(null, mimeType)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed reading media from uri: $uri", e)
            Pair(null, mimeType)
        }
    }
}
