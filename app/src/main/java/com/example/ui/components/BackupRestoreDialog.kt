package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ceylonsteps.travelapp.data.model.TripWithStops
import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import com.example.ui.theme.BentoAmberContainer
import com.example.ui.theme.BentoAmberSecondary
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoCyanAccent
import com.example.ui.theme.BentoGreenAccent
import com.example.ui.theme.BentoLavenderContainer
import com.example.ui.theme.BentoMintAccent
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoRoseContainer
import com.example.util.BackupData
import com.example.util.BackupTier
import com.example.util.DatabaseBackupManager
import com.example.util.GoogleDriveBackupEngine
import com.example.util.RestoreResult
import com.example.worker.AutoBackupManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreDialog(
    userProfile: UserProfile,
    trips: List<TripLocation>,
    journeys: List<TripWithStops>,
    isLoading: Boolean,
    lastResult: RestoreResult?,
    onDismissRequest: () -> Unit,
    onRestoreBackup: (jsonContent: String, overwrite: Boolean, restoreProfile: Boolean) -> Unit,
    onClearResult: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPreferences = remember { context.getSharedPreferences("CeylonStepsPrefs", android.content.Context.MODE_PRIVATE) }
    var isAutoSyncEnabled by remember { mutableStateOf(sharedPreferences.getBoolean("auto_backup_enabled", false)) }
    var autoSyncInterval by remember { mutableIntStateOf(sharedPreferences.getInt("auto_backup_interval", 1)) }

    // 0: Google Drive, 1: Export JSON, 2: Import JSON
    var selectedTab by remember { mutableIntStateOf(0) }
    var isCloudWorking by remember { mutableStateOf(false) }
    var cloudStatusText by remember { mutableStateOf("") }

    // Import Preview State
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var loadedJsonContent by remember { mutableStateOf<String?>(null) }
    var parsedBackupData by remember { mutableStateOf<BackupData?>(null) }
    var parseErrorMessage by remember { mutableStateOf<String?>(null) }

    // Restore Options
    var overwriteExisting by remember { mutableStateOf(false) } // Default: Merge
    var restoreUserProfile by remember { mutableStateOf(true) }

    val googleAccount = remember(selectedTab, isCloudWorking) {
        GoogleSignIn.getLastSignedInAccount(context)
    }

    // SAF Document Creator (Export JSON)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val jsonString = DatabaseBackupManager.exportToJson(
                userProfile = userProfile,
                tripLocations = trips,
                multiStopJourneys = journeys
            )
            val success = DatabaseBackupManager.writeJsonToUri(context, uri, jsonString)
            if (success) {
                Toast.makeText(context, "Backup exported successfully to JSON!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to write backup file.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // SAF Document Opener (Import JSON)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            onClearResult()
            val content = DatabaseBackupManager.readJsonFromUri(context, uri)
            if (content != null) {
                loadedJsonContent = content
                try {
                    val parsed = DatabaseBackupManager.parseJson(content)
                    parsedBackupData = parsed
                    parseErrorMessage = null
                } catch (e: Exception) {
                    parsedBackupData = null
                    parseErrorMessage = "Invalid JSON structure: ${e.localizedMessage ?: "Parsing error"}"
                }
            } else {
                parseErrorMessage = "Could not read data from selected file."
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("backup_restore_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Badge & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BentoPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Backup & Restore",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cloud Drive Sync & Offline File Transfer",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Modern Segmented Pill Tabs
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ModernTabButton(
                            title = "Google Drive",
                            icon = Icons.Default.CloudSync,
                            isSelected = selectedTab == 0,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 0 }
                        )
                        ModernTabButton(
                            title = "Export JSON",
                            icon = Icons.Default.FileUpload,
                            isSelected = selectedTab == 1,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 1 }
                        )
                        ModernTabButton(
                            title = "Import JSON",
                            icon = Icons.Default.FileDownload,
                            isSelected = selectedTab == 2,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 2 }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Result Notification Card if any
                if (lastResult != null) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (lastResult.isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (lastResult.isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (lastResult.isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = lastResult.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (lastResult.isSuccess) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // ==========================================
                // TAB 0: GOOGLE DRIVE CLOUD SYNC & RESTORE
                // ==========================================
                if (selectedTab == 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Signed-in Account Status Banner
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (googleAccount != null) BentoPrimaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, if (googleAccount != null) BentoPrimary.copy(alpha = 0.3f) else BentoBorderLight.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (googleAccount != null) BentoPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = if (googleAccount != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (googleAccount != null) (googleAccount.displayName ?: "Connected Account") else "Not Signed In to Google",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (googleAccount != null) (googleAccount.email ?: "Google Drive Ready") else "Sign in from Profile to enable Google Drive Cloud sync",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (googleAccount != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = BentoGreenAccent.copy(alpha = 0.15f),
                                        modifier = Modifier.padding(start = 6.dp)
                                    ) {
                                        Text(
                                            text = "Connected",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // SECTION 1: RESTORE FROM DRIVE (HERO CARD)
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, BentoPrimary.copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = BentoPrimary,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDownload,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Restore from Google Drive",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Download your saved cloud backup to this device",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isCloudWorking && cloudStatusText.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = BentoPrimary.copy(alpha = 0.08f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = BentoPrimary
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = cloudStatusText,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                color = BentoPrimary
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        val account = GoogleSignIn.getLastSignedInAccount(context)
                                        if (account == null) {
                                            Toast.makeText(context, "Please Sign In to Google Account first in Profile.", Toast.LENGTH_LONG).show()
                                        } else {
                                            isCloudWorking = true
                                            cloudStatusText = "Connecting to Google Drive..."
                                            scope.launch {
                                                val jsonString = GoogleDriveBackupEngine.restoreFromDrive(
                                                    context, account
                                                ) { _, message ->
                                                    cloudStatusText = message
                                                }
                                                isCloudWorking = false
                                                if (jsonString != null) {
                                                    loadedJsonContent = jsonString
                                                    try {
                                                        val parsed = DatabaseBackupManager.parseJson(jsonString)
                                                        parsedBackupData = parsed
                                                        onRestoreBackup(jsonString, false, true)
                                                        Toast.makeText(context, "Successfully restored from Google Drive!", Toast.LENGTH_LONG).show()
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Downloaded file parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "No backup file found in Google Drive.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isCloudWorking,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("btn_restore_from_google_drive"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                                ) {
                                    if (isCloudWorking) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Restoring Cloud Backup...", fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Download & Restore Now", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // SECTION 2: BACKUP TO GOOGLE DRIVE TIERS
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "UPLOAD NEW CLOUD BACKUP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = BentoPrimary
                                )
                            )

                            // 3 Backup Tiers
                            BackupTier.entries.forEach { tier ->
                                val (title, icon, accentColor) = when (tier) {
                                    BackupTier.DATA_ONLY -> Triple("Basic Data Sync", Icons.Default.CloudSync, BentoMintAccent)
                                    BackupTier.DATA_WITH_IMAGES -> Triple("Data + Memories (Photos)", Icons.Default.PhotoLibrary, BentoAmberSecondary)
                                    BackupTier.FULL_BACKUP -> Triple("Complete Archive (Full)", Icons.Default.Storage, BentoPrimary)
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            val account = GoogleSignIn.getLastSignedInAccount(context)
                                            if (account == null) {
                                                Toast.makeText(context, "Please Sign In to Google first in Profile.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                isCloudWorking = true
                                                cloudStatusText = "Uploading to Google Drive..."
                                                scope.launch {
                                                    val result = GoogleDriveBackupEngine.performBackup(
                                                        context, account, tier, trips, journeys
                                                    ) { _, message ->
                                                        cloudStatusText = message
                                                    }
                                                    isCloudWorking = false
                                                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = accentColor.copy(alpha = 0.12f),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = accentColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = tier.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = "Upload",
                                            tint = BentoPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // SECTION 3: AUTOMATIC CLOUD SYNC CONFIG
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = BentoPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Automatic Cloud Sync",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Periodically backs up trips to Google Drive",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isAutoSyncEnabled,
                                        onCheckedChange = { checked ->
                                            isAutoSyncEnabled = checked
                                            sharedPreferences.edit().putBoolean("auto_backup_enabled", checked).apply()
                                            if (checked) {
                                                AutoBackupManager.scheduleAutoBackup(context, autoSyncInterval.toLong())
                                            } else {
                                                AutoBackupManager.cancelAutoBackup(context)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = BentoPrimary
                                        )
                                    )
                                }

                                if (isAutoSyncEnabled) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IntervalPill(
                                            title = "Daily Backup",
                                            isSelected = autoSyncInterval == 1,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                autoSyncInterval = 1
                                                sharedPreferences.edit().putInt("auto_backup_interval", 1).apply()
                                                AutoBackupManager.scheduleAutoBackup(context, 1)
                                            }
                                        )
                                        IntervalPill(
                                            title = "Weekly Backup",
                                            isSelected = autoSyncInterval == 7,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                autoSyncInterval = 7
                                                sharedPreferences.edit().putInt("auto_backup_interval", 7).apply()
                                                AutoBackupManager.scheduleAutoBackup(context, 7)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // TAB 1: EXPORT TO JSON (LOCAL OFFLINE FILE)
                // ==========================================
                if (selectedTab == 1) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "LOCAL DATABASE SUMMARY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = BentoPrimary
                            )
                        )

                        // 4-item Bento Grid for Current Database
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BentoStatCard(
                                title = "Footprints",
                                count = "${trips.size}",
                                subtitle = "Locations saved",
                                icon = Icons.Default.Place,
                                iconColor = BentoMintAccent,
                                modifier = Modifier.weight(1f)
                            )
                            BentoStatCard(
                                title = "Journeys",
                                count = "${journeys.size}",
                                subtitle = "Expeditions",
                                icon = Icons.Default.Route,
                                iconColor = BentoAmberSecondary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val totalWaypoints = journeys.sumOf { it.stops.size }
                            BentoStatCard(
                                title = "Waypoints",
                                count = "$totalWaypoints",
                                subtitle = "Stops & photos",
                                icon = Icons.Default.PhotoLibrary,
                                iconColor = BentoCyanAccent,
                                modifier = Modifier.weight(1f)
                            )
                            BentoStatCard(
                                title = "Home Base",
                                count = userProfile.homeLocationName.ifBlank { "Sri Lanka" }.take(10),
                                subtitle = userProfile.userName.ifBlank { "Explorer" }.take(12),
                                icon = Icons.Default.AccountCircle,
                                iconColor = BentoPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Export Button 1: Save file to device
                        Button(
                            onClick = {
                                val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                val defaultName = "CeylonSteps_Backup_$dateStr.json"
                                createDocumentLauncher.launch(defaultName)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_export_json_file"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save JSON Backup File", fontWeight = FontWeight.Bold)
                        }

                        // Export Button 2: Share / Send JSON
                        OutlinedButton(
                            onClick = {
                                val jsonString = DatabaseBackupManager.exportToJson(
                                    userProfile = userProfile,
                                    tripLocations = trips,
                                    multiStopJourneys = journeys
                                )
                                val shareIntent = DatabaseBackupManager.createShareIntent(context, jsonString)
                                context.startActivity(Intent.createChooser(shareIntent, "Share CeylonSteps Backup"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_share_json_backup"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BentoBorderLight)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = BentoPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share via WhatsApp, Drive or Email", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        // Privacy footnote
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "100% Offline & Private JSON File. Safe to store anywhere.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // ==========================================
                // TAB 2: IMPORT & RESTORE FROM JSON FILE
                // ==========================================
                if (selectedTab == 2) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Pick File Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.5.dp, BentoPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                }
                                .testTag("btn_select_json_backup")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = BentoPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null,
                                            tint = BentoPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (selectedFileUri != null) "File Selected (Tap to Change)" else "Choose JSON Backup File",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BentoPrimary
                                )
                                Text(
                                    text = if (selectedFileUri != null) (selectedFileUri?.lastPathSegment ?: "backup.json") else "Browse files from device storage or Google Drive",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (parseErrorMessage != null) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                            ) {
                                Text(
                                    text = parseErrorMessage ?: "",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFC62828))
                                )
                            }
                        }

                        // Parsed Backup Preview
                        if (parsedBackupData != null) {
                            val backup = parsedBackupData!!
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "BACKUP FILE CONTENTS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                color = BentoPrimary
                                            )
                                        )
                                    }

                                    val dateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(backup.exportedAtEpoch))
                                    DetailRow(label = "Backup Date", value = dateFormatted)
                                    DetailRow(label = "Footprints / Trips", value = "${backup.tripLocations.size} locations")
                                    DetailRow(label = "Multi-Stop Journeys", value = "${backup.multiStopJourneys.size} expeditions")

                                    if (backup.userProfile != null) {
                                        DetailRow(
                                            label = "Traveler Profile",
                                            value = "${backup.userProfile.userName} (${backup.userProfile.homeLocationName})"
                                        )
                                    }
                                }
                            }

                            // Import Strategy Choice
                            Text(
                                text = "RESTORE STRATEGY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = BentoPrimary
                                )
                            )

                            // Option 1: Merge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (!overwriteExisting) BentoPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (!overwriteExisting) BentoPrimary else BentoBorderLight.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { overwriteExisting = false }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = !overwriteExisting,
                                        onClick = { overwriteExisting = false },
                                        colors = RadioButtonDefaults.colors(selectedColor = BentoPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text("Merge with Existing Data (Recommended)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text("Appends backup items without deleting your current records", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    }
                                }
                            }

                            // Option 2: Overwrite
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (overwriteExisting) BentoPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (overwriteExisting) BentoPrimary else BentoBorderLight.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { overwriteExisting = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = overwriteExisting,
                                        onClick = { overwriteExisting = true },
                                        colors = RadioButtonDefaults.colors(selectedColor = BentoPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text("Replace / Clean Restore", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text("Replaces existing data with the exact records from this backup", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    }
                                }
                            }

                            // Option 3: Restore Profile Checkbox
                            if (backup.userProfile != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = restoreUserProfile,
                                        onCheckedChange = { restoreUserProfile = it },
                                        colors = CheckboxDefaults.colors(checkedColor = BentoPrimary)
                                    )
                                    Text(
                                        "Also restore Traveler Name & Home Location",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Action Button: Restore
                            Button(
                                onClick = {
                                    if (loadedJsonContent != null) {
                                        onRestoreBackup(loadedJsonContent!!, overwriteExisting, restoreUserProfile)
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_confirm_restore_backup"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Restoring Database...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Restore, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Restore Database Now", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER BENTO & PILL COMPONENTS
// -------------------------------------------------------------

@Composable
private fun ModernTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) BentoPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = if (isSelected) BentoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BentoStatCard(
    title: String,
    count: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IntervalPill(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BentoPrimary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) BentoPrimary else BentoBorderLight.copy(alpha = 0.6f)),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
