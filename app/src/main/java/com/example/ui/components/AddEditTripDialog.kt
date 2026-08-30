package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.SriLankaDestinations
import com.example.data.model.TripLocation
import com.example.data.repository.TripRepository
import com.example.ui.theme.BentoAmberSecondary
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoLavenderContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTripDialog(
    tripToEdit: TripLocation?,
    onDismiss: () -> Unit,
    onPickFromMap: (TripLocation) -> Unit,
    onSave: (
        id: Long,
        title: String,
        description: String,
        lat: Double,
        lng: Double,
        locationName: String,
        dateEpochMillis: Long,
        isUpcoming: Boolean,
        imageUris: List<String>,
        coverImageUri: String?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var title by remember { mutableStateOf(tripToEdit?.title ?: "") }
    var locationName by remember { mutableStateOf(tripToEdit?.locationName ?: "") }
    var description by remember { mutableStateOf(tripToEdit?.description ?: "") }
    var latText by remember { mutableStateOf(tripToEdit?.latitude?.toString() ?: "7.9570") }
    var lngText by remember { mutableStateOf(tripToEdit?.longitude?.toString() ?: "80.7603") }
    var isUpcoming by remember { mutableStateOf(tripToEdit?.isUpcoming ?: false) }
    var dateEpochMillis by remember { mutableLongStateOf(tripToEdit?.dateEpochMillis ?: System.currentTimeMillis()) }

    // Province Selection state
    var selectedProvince by remember {
        val initialLat = tripToEdit?.latitude ?: 7.9570
        val initialLng = tripToEdit?.longitude ?: 80.7603
        mutableStateOf(SriLankaDestinations.findMatchingProvince(initialLat, initialLng))
    }
    var isProvinceDropdownOpen by remember { mutableStateOf(false) }

    // Media list (Device photos & videos)
    var photosList by remember {
        mutableStateOf(
            tripToEdit?.let { TripRepository.parseJsonArray(it.imageUrisJson) } ?: emptyList()
        )
    }
    var coverUri by remember { mutableStateOf(tripToEdit?.coverImageUri ?: photosList.firstOrNull()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Photo picker launcher from device
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newUris = uris.map { it.toString() }.filter { !photosList.contains(it) }
            photosList = photosList + newUris
            if (coverUri == null && newUris.isNotEmpty()) {
                coverUri = newUris.first()
            }
        }
    }

    // Video picker launcher from device
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newUris = uris.map { it.toString() }.filter { !photosList.contains(it) }
            photosList = photosList + newUris
            if (coverUri == null && newUris.isNotEmpty()) {
                coverUri = newUris.first()
            }
        }
    }

    // Auto-sync province if coordinates change
    LaunchedEffect(latText, lngText) {
        val lat = latText.toDoubleOrNull()
        val lng = lngText.toDoubleOrNull()
        if (lat != null && lng != null) {
            val matched = SriLankaDestinations.findMatchingProvince(lat, lng)
            selectedProvince = matched
        }
    }

    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)

    fun getCurrentDraft(): TripLocation {
        val lat = latText.toDoubleOrNull() ?: 7.9570
        val lng = lngText.toDoubleOrNull() ?: 80.7603
        return TripLocation(
            id = tripToEdit?.id ?: 0L,
            title = title,
            description = description,
            latitude = lat,
            longitude = lng,
            locationName = locationName.ifBlank { "$selectedProvince Province, Sri Lanka" },
            dateEpochMillis = dateEpochMillis,
            isUpcoming = isUpcoming,
            imageUrisJson = TripRepository.toJsonArray(photosList),
            coverImageUri = coverUri ?: photosList.firstOrNull()
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = Modifier.testTag("add_edit_trip_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BentoPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (tripToEdit != null && tripToEdit.id != 0L) Icons.Default.Place else Icons.Default.AddLocation,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (tripToEdit != null && tripToEdit.id != 0L) "Edit Travel Footprint" else "Add New Single Trip",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                        )
                        Text(
                            text = "Record memories, photos & videos across Sri Lanka",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Visited vs Upcoming Plan Toggle Selector
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Visited Option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isUpcoming) BentoPrimary else Color.Transparent)
                            .clickable { isUpcoming = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ Visited Footprint",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (!isUpcoming) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // Upcoming Option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isUpcoming) BentoAmberSecondary else Color.Transparent)
                            .clickable { isUpcoming = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "★ Upcoming Adventure",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isUpcoming) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Quick Preset Sri Lanka Spots Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Quick Sri Lanka Landmarks",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SriLankaDestinations.PRESET_LANDMARKS.take(15)) { landmark ->
                        FilterChip(
                            selected = locationName.contains(landmark.locationName, ignoreCase = true) || title.contains(landmark.title, ignoreCase = true),
                            onClick = {
                                title = landmark.title
                                locationName = landmark.locationName
                                selectedProvince = landmark.province
                                latText = landmark.latitude.toString()
                                lngText = landmark.longitude.toString()
                                description = landmark.description
                            },
                            shape = RoundedCornerShape(12.dp),
                            label = { Text(landmark.title, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BentoLavenderContainer,
                                selectedLabelColor = BentoPrimary
                            )
                        )
                    }
                }
            }

            // 1. Your Visit Name (Renamed as requested)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Your Visit Name") },
                placeholder = { Text("e.g. Sigiriya Sunrise, Galle Fort Sunset, Ella Hike") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_trip_title"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BentoPrimary
                )
            )

            // 2. Location Name
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Location Name") },
                placeholder = { Text("e.g. Sigiriya, Ella, Mirissa, Nuwara Eliya") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = BentoPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_location_name"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // 3. Province Selection Dropdown (Requested right after Location Name)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Province (පළාත)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isProvinceDropdownOpen = true }
                            .border(
                                width = 1.dp,
                                color = BentoBorderLight,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .testTag("select_province_button"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "$selectedProvince Province",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = SriLankaDestinations.PROVINCE_SINHALA[selectedProvince] ?: "",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = BentoPrimaryDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Province",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isProvinceDropdownOpen,
                        onDismissRequest = { isProvinceDropdownOpen = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        SriLankaDestinations.PROVINCES.forEach { prov ->
                            val sinhala = SriLankaDestinations.PROVINCE_SINHALA[prov] ?: ""
                            val isSel = selectedProvince == prov
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$prov Province",
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) BentoPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = sinhala,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProvince = prov
                                    isProvinceDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }

            // 4. Map Location Picker Trigger & Geospatial Coordinates
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Map Location & Coordinates",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Map Icon Button to select on Map
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onPickFromMap(getCurrentDraft())
                            }
                            .testTag("btn_select_location_on_map"),
                        shape = RoundedCornerShape(12.dp),
                        color = BentoPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Select Location on Map",
                                tint = BentoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Select on Map",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimary
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_latitude"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Longitude") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_longitude"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // Date Picker Trigger
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showDatePicker = true }
                    .border(
                        width = 1.dp,
                        color = BentoBorderLight,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .testTag("btn_date_picker"),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = BentoPrimary
                        )
                        Column {
                            Text(
                                text = if (isUpcoming) "Scheduled Date" else "Date Visited",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = dateFormat.format(Date(dateEpochMillis)),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary
                        )
                    )
                }
            }

            // Travel Journal Notes
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Travel Journal Notes & Highlights") },
                placeholder = { Text("Write your Sri Lanka travel reflections, local food, culture, nature trails...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("input_trip_notes"),
                maxLines = 4,
                shape = RoundedCornerShape(14.dp)
            )

            // 5. Device Photos & Videos Gallery (No online image input as requested)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Device Photos & Videos",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Pick from Device Buttons (Photos & Videos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_add_device_photos"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Photos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { videoPickerLauncher.launch("video/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_add_device_videos"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Media Gallery List (Horizontal Scroll)
                if (photosList.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        items(photosList) { uriStr ->
                            val isCover = coverUri == uriStr
                            val isVideo = uriStr.contains("video", ignoreCase = true) || uriStr.endsWith(".mp4", ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = if (isCover) 3.dp else 1.dp,
                                        color = if (isCover) BentoPrimary else BentoBorderLight,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { coverUri = uriStr }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uriStr)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Media Item",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                if (isVideo) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = "Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                if (isCover) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BentoPrimary,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = "Cover",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }

                                // Delete media item button
                                IconButton(
                                    onClick = {
                                        photosList = photosList.filter { it != uriStr }
                                        if (coverUri == uriStr) {
                                            coverUri = photosList.firstOrNull()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Media",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No device media attached yet. Tap above to add photos & videos from your phone.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Save Action Button
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull() ?: 7.8731
                    val lng = lngText.toDoubleOrNull() ?: 80.7718
                    val finalLocName = locationName.ifBlank { "$selectedProvince Province, Sri Lanka" }
                    val finalTitle = title.ifBlank { finalLocName }

                    onSave(
                        tripToEdit?.id ?: 0L,
                        finalTitle,
                        description,
                        lat,
                        lng,
                        finalLocName,
                        dateEpochMillis,
                        isUpcoming,
                        photosList,
                        coverUri ?: photosList.firstOrNull()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_trip"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUpcoming) BentoAmberSecondary else BentoPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (tripToEdit != null && tripToEdit.id != 0L) "Update Single Trip" else "Save Single Trip",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateEpochMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dateEpochMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm", color = BentoPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
