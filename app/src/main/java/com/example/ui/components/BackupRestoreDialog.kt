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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoLavenderContainer
import com.example.ui.theme.BentoMintAccent
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.util.BackupData
import com.example.util.DatabaseBackupManager
import com.example.util.RestoreResult
import com.lankafootprints.travelapp.data.model.TripWithStops
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Export, 1: Import

    // Import Preview State
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var loadedJsonContent by remember { mutableStateOf<String?>(null) }
    var parsedBackupData by remember { mutableStateOf<BackupData?>(null) }
    var parseErrorMessage by remember { mutableStateOf<String?>(null) }

    // Restore Options
    var overwriteExisting by remember { mutableStateOf(false) } // Default: Merge
    var restoreUserProfile by remember { mutableStateOf(true) }

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

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backup_restore_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BentoLavenderContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = null,
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Local Database Backup",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Export & Import JSON Backups",
                                style = MaterialTheme.typography.bodySmall.copy(color = BentoPrimary)
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; onClearResult() },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export JSON", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; onClearResult() },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import & Restore", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; onClearResult() },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Google Drive", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
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

                // TAB 0: EXPORT TO JSON
                if (selectedTab == 0) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CURRENT DATABASE CONTENTS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = BentoPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Travel Footprints / Trips:", style = MaterialTheme.typography.bodyMedium)
                                Text("${trips.size} locations", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Multi-Stop Journeys:", style = MaterialTheme.typography.bodyMedium)
                                Text("${journeys.size} expeditions", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val totalWaypoints = journeys.sumOf { it.stops.size }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Itinerary Waypoints & Media:", style = MaterialTheme.typography.bodyMedium)
                                Text("$totalWaypoints stops", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("User Profile & Home Base:", style = MaterialTheme.typography.bodyMedium)
                                Text(userProfile.homeLocationName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Export Action 1: Save to storage
                    Button(
                        onClick = {
                            val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            val defaultName = "LankaFootprints_Backup_$dateStr.json"
                            createDocumentLauncher.launch(defaultName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_export_json_file"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save JSON Backup to Device", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Export Action 2: Share / Send JSON
                    OutlinedButton(
                        onClick = {
                            val jsonString = DatabaseBackupManager.exportToJson(
                                userProfile = userProfile,
                                tripLocations = trips,
                                multiStopJourneys = journeys
                            )
                            val shareIntent = DatabaseBackupManager.createShareIntent(context, jsonString)
                            context.startActivity(Intent.createChooser(shareIntent, "Share LankaFootprints Backup"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_share_json_backup"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BentoBorderLight)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = BentoPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share / Send Backup File", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // TAB 1: IMPORT & RESTORE FROM JSON
                if (selectedTab == 1) {
                    // Step 1: Pick File Button
                    OutlinedButton(
                        onClick = {
                            openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_select_json_backup"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BentoPrimary)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = BentoPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedFileUri != null) "Select Different JSON File" else "Browse & Select JSON Backup File",
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary
                        )
                    }

                    if (parseErrorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
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

                    // Step 2: Backup Preview Card
                    if (parsedBackupData != null) {
                        val backup = parsedBackupData!!
                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
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

                                Spacer(modifier = Modifier.height(10.dp))

                                val dateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(backup.exportedAtEpoch))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Backup Date:", style = MaterialTheme.typography.bodySmall)
                                    Text(dateFormatted, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Single Trip Locations:", style = MaterialTheme.typography.bodySmall)
                                    Text("${backup.tripLocations.size} items", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Multi-Stop Journeys:", style = MaterialTheme.typography.bodySmall)
                                    Text("${backup.multiStopJourneys.size} items", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }

                                if (backup.userProfile != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Traveler / Home Base:", style = MaterialTheme.typography.bodySmall)
                                        Text("${backup.userProfile.userName} (${backup.userProfile.homeLocationName})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Restore Mode Selection
                        Text(
                            text = "IMPORT STRATEGY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = BentoPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Option 1: Merge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !overwriteExisting,
                                onClick = { overwriteExisting = false },
                                colors = RadioButtonDefaults.colors(selectedColor = BentoPrimary)
                            )
                            Column {
                                Text("Merge with Existing Data", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("Appends backup items without deleting your current records", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                        }

                        // Option 2: Overwrite
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = overwriteExisting,
                                onClick = { overwriteExisting = true },
                                colors = RadioButtonDefaults.colors(selectedColor = BentoPrimary)
                            )
                            Column {
                                Text("Replace / Clean Restore", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("Replaces existing data with the exact state from this backup", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                        }

                        // Option 3: Restore Profile
                        if (backup.userProfile != null) {
                            Spacer(modifier = Modifier.height(6.dp))
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
                                    "Also restore User Profile & Home Base coordinates",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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
                                Text("Restoring Database...")
                            } else {
                                Icon(Icons.Default.Restore, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Restore Backup to Room Database", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // TAB 2: GOOGLE DRIVE CLOUD SYNC
                if (selectedTab == 2) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Cloud Backup Engine",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary
                        )

                        // 3 Tiers of Backup
                        com.example.util.BackupTier.entries.forEach { tier ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // In real implementation, this triggers the backup via GoogleDriveBackupEngine
                                        // after verifying Google Sign-In status and requesting permissions if needed.
                                        Toast.makeText(context, "Initiating ${tier.title} to Google Drive...", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, BentoBorderLight)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = tier.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = tier.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
