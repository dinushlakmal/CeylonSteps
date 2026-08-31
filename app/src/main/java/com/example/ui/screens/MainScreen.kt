package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.SriLankaDestinations
import com.example.data.repository.TripRepository
import com.example.ui.components.AddEditMultiStopTripDialog
import com.example.ui.components.AddEditTripDialog
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.CustomCalendarView
import com.example.ui.components.EditProfileBottomSheet
import com.example.ui.components.MediaViewerDialog
import com.example.ui.components.OfflineMapCacheDialog
import com.example.ui.components.OsmMapView
import com.example.ui.components.SriLankaExplorerView
import com.example.ui.components.StatsHeaderCard
import com.example.ui.components.TripCard
import com.example.ui.components.TripDetailBottomSheet
import com.example.ui.components.TripTimelineCard
import com.example.ui.components.isVideoMedia
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.BentoActiveIndicator
import com.example.ui.theme.BentoAmberSecondary
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoDeepPurple
import com.example.ui.theme.BentoLavenderBorder
import com.example.ui.theme.BentoLavenderContainer
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoSkyBlue
import com.example.ui.theme.UpcomingBadgeColor
import com.example.ui.theme.VisitedBadgeColor
import com.example.util.GeoDistanceEngine
import com.example.ui.viewmodel.FilterTab
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.TripViewModel
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TripViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSearchOpen by remember { mutableStateOf(false) }
    var isProvinceMenuOpen by remember { mutableStateOf(false) }
    var useTopoMap by remember { mutableStateOf(false) }
    var currentMapView by remember { mutableStateOf<MapView?>(null) }
    var isFabMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchOpen) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search journeys, stops, cities...", fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                                .testTag("search_text_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.setSearchQuery("")
                                    isSearchOpen = false
                                }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close search")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPrimary,
                                unfocusedBorderColor = BentoBorderLight.copy(alpha = 0.6f)
                            )
                        )
                    } else {
                        com.example.ui.components.CeylonStepsBrand()
                    }
                },
                actions = {
                    if (!isSearchOpen) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isSearchOpen = true }
                                .testTag("btn_search_toggle")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Province Filter Menu
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.selectedProvince != null) BentoAmberSecondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (uiState.selectedProvince != null) BorderStroke(1.dp, BentoAmberSecondary) else null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isProvinceMenuOpen = true }
                                .testTag("btn_province_filter")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter Province",
                                    tint = if (uiState.selectedProvince != null) BentoAmberSecondary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isProvinceMenuOpen,
                            onDismissRequest = { isProvinceMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All 9 Provinces") },
                                onClick = {
                                    viewModel.setSelectedProvince(null)
                                    isProvinceMenuOpen = false
                                }
                            )
                            SriLankaDestinations.PROVINCES.forEach { prov ->
                                val sinhala = SriLankaDestinations.PROVINCE_SINHALA[prov] ?: ""
                                DropdownMenuItem(
                                    text = { Text(if (sinhala.isNotBlank()) "$prov ($sinhala)" else "$prov Province") },
                                    onClick = {
                                        viewModel.setSelectedProvince(prov)
                                        isProvinceMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    // Map layer switch if in Map view
                    if (uiState.activeNavigationTab == NavigationTab.MAP) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (useTopoMap) BentoPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (useTopoMap) BorderStroke(1.dp, BentoPrimary) else null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { useTopoMap = !useTopoMap }
                                .testTag("btn_map_layer")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Toggle Map Layer",
                                    tint = if (useTopoMap) BentoPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Quick Theme Toggle Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val nextMode = when (uiState.themeMode) {
                                    com.example.data.repository.ThemeMode.SYSTEM -> com.example.data.repository.ThemeMode.DARK
                                    com.example.data.repository.ThemeMode.DARK -> com.example.data.repository.ThemeMode.LIGHT
                                    com.example.data.repository.ThemeMode.LIGHT -> com.example.data.repository.ThemeMode.SYSTEM
                                }
                                viewModel.setThemeMode(nextMode)
                            }
                            .testTag("btn_quick_theme_toggle")
                    ) {
                        val icon = when (uiState.themeMode) {
                            com.example.data.repository.ThemeMode.DARK -> Icons.Default.Brightness4
                            com.example.data.repository.ThemeMode.LIGHT -> Icons.Default.Brightness7
                            com.example.data.repository.ThemeMode.SYSTEM -> Icons.Default.BrightnessMedium
                        }
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Toggle Theme Mode (${uiState.themeMode.name})",
                                tint = BentoAmberSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Bento User Avatar Pill
                    Surface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.setNavigationTab(NavigationTab.PROFILE) }
                            .testTag("btn_top_avatar_profile"),
                        shape = CircleShape,
                        color = BentoLavenderContainer,
                        border = BorderStroke(1.dp, BentoLavenderBorder)
                    ) {
                        if (uiState.userProfile.profileImageUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uiState.userProfile.profileImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "User Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                val initials = uiState.userProfile.userName.split(" ")
                                    .filter { it.isNotBlank() }
                                    .take(2)
                                    .map { it.first().uppercaseChar() }
                                    .joinToString("")
                                    .ifBlank { "LF" }
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BentoOnPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .border(
                        BorderStroke(
                            1.dp,
                            BentoBorderLight.copy(alpha = 0.4f)
                        )
                    )
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                NavigationBarItem(
                    selected = uiState.activeNavigationTab == NavigationTab.MAP,
                    onClick = { viewModel.setNavigationTab(NavigationTab.MAP) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeNavigationTab == NavigationTab.MAP) Icons.Filled.Map else Icons.Outlined.Map,
                            contentDescription = "Map"
                        )
                    },
                    label = { Text("Map", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoOnPrimaryContainer,
                        selectedTextColor = BentoOnPrimaryContainer,
                        indicatorColor = BentoActiveIndicator
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeNavigationTab == NavigationTab.TIMELINE,
                    onClick = { viewModel.setNavigationTab(NavigationTab.TIMELINE) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeNavigationTab == NavigationTab.TIMELINE) Icons.Filled.Route else Icons.Outlined.Route,
                            contentDescription = "Journeys"
                        )
                    },
                    label = { Text("Journeys", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoOnPrimaryContainer,
                        selectedTextColor = BentoOnPrimaryContainer,
                        indicatorColor = BentoActiveIndicator
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeNavigationTab == NavigationTab.JOURNAL,
                    onClick = { viewModel.setNavigationTab(NavigationTab.JOURNAL) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeNavigationTab == NavigationTab.JOURNAL) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = "Journal"
                        )
                    },
                    label = { Text("Journal", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoOnPrimaryContainer,
                        selectedTextColor = BentoOnPrimaryContainer,
                        indicatorColor = BentoActiveIndicator
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeNavigationTab == NavigationTab.CALENDAR,
                    onClick = { viewModel.setNavigationTab(NavigationTab.CALENDAR) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeNavigationTab == NavigationTab.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Calendar"
                        )
                    },
                    label = { Text("Calendar", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoOnPrimaryContainer,
                        selectedTextColor = BentoOnPrimaryContainer,
                        indicatorColor = BentoActiveIndicator
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeNavigationTab == NavigationTab.EXPLORER,
                    onClick = { viewModel.setNavigationTab(NavigationTab.EXPLORER) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeNavigationTab == NavigationTab.EXPLORER) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = "Explorer"
                        )
                    },
                    label = { Text("Explorer", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoOnPrimaryContainer,
                        selectedTextColor = BentoOnPrimaryContainer,
                        indicatorColor = BentoActiveIndicator
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeNavigationTab == NavigationTab.PROFILE,
                    onClick = { viewModel.setNavigationTab(NavigationTab.PROFILE) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeNavigationTab == NavigationTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoOnPrimaryContainer,
                        selectedTextColor = BentoOnPrimaryContainer,
                        indicatorColor = BentoActiveIndicator
                    )
                )
            }
        },
        floatingActionButton = {
            if (!uiState.isMapPickerMode) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isFabMenuOpen) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, BentoBorderLight)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isFabMenuOpen = false
                                        viewModel.openMultiStopBuilder()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("fab_option_multi_stop")
                                ) {
                                    Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("+ Plan Trip", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        isFabMenuOpen = false
                                        viewModel.openAddTripDialog()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("fab_option_single_stop")
                                ) {
                                    Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("+ Single Visit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { isFabMenuOpen = !isFabMenuOpen },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("fab_add_trip"),
                        shape = RoundedCornerShape(20.dp),
                        containerColor = BentoPrimary,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isFabMenuOpen) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Add Trip Options",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeNavigationTab) {
                NavigationTab.MAP -> {
                    // Interactive OpenStreetMap (osmdroid) View with Marked Locations Only
                    Box(modifier = Modifier.fillMaxSize()) {
                        OsmMapView(
                            trips = uiState.filteredTrips,
                            selectedTrip = uiState.selectedTrip,
                            activeJourney = uiState.activeJourneyOnMap,
                            centerTarget = uiState.mapCenterTarget,
                            targetZoom = uiState.mapTargetZoom,
                            isPickerMode = uiState.isMapPickerMode,
                            pickedCoordinates = uiState.pickedCoordinates,
                            homeLocation = Pair(uiState.userProfile.homeLatitude, uiState.userProfile.homeLongitude),
                            userPhotoUrl = uiState.userProfile.profileImageUri,
                            onTripSelected = { trip ->
                                viewModel.selectTrip(trip)
                            },
                            onLocationPicked = { lat, lng -> viewModel.onMapLocationPicked(lat, lng) },
                            useTopoMap = useTopoMap,
                            onMapViewReady = { mv -> currentMapView = mv }
                        )

                        // Floating Selected Location Preview Card on Map Screen
                        val selTrip = uiState.selectedTrip
                        if (selTrip != null && !uiState.isMapPickerMode && uiState.activeJourneyOnMap == null) {
                            val selPhotos = remember(selTrip.imageUrisJson) { TripRepository.parseJsonArray(selTrip.imageUrisJson) }
                            val selCover = selTrip.coverImageUri ?: selPhotos.firstOrNull()
                            val selProvince = SriLankaDestinations.findMatchingProvince(selTrip.latitude, selTrip.longitude)
                            val selSinhalaProv = SriLankaDestinations.PROVINCE_SINHALA[selProvince] ?: ""
                            val isCoverVid = isVideoMedia(selCover ?: "")

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp, start = 14.dp, end = 14.dp)
                                    .fillMaxWidth()
                                    .testTag("map_selected_trip_card"),
                                shape = RoundedCornerShape(22.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                shadowElevation = 10.dp,
                                border = BorderStroke(1.5.dp, BentoPrimary.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Thumbnail / Cover Image with Play Badge & Click-to-open-media
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    if (selPhotos.isNotEmpty()) {
                                                        viewModel.openMediaViewer(selPhotos, 0, selTrip.title, selTrip.locationName)
                                                    } else if (!selCover.isNullOrBlank()) {
                                                        viewModel.openMediaViewer(listOf(selCover), 0, selTrip.title, selTrip.locationName)
                                                    } else {
                                                        viewModel.openTripDetail(selTrip)
                                                    }
                                                }
                                        ) {
                                            if (!selCover.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(selCover)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = selTrip.title,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                if (isCoverVid) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.35f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Play",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Brush.linearGradient(listOf(BentoPrimary, BentoAmberSecondary))),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Place,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }

                                            if (selPhotos.size > 1) {
                                                Surface(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(4.dp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color.Black.copy(alpha = 0.7f)
                                                ) {
                                                    Text(
                                                        text = "+${selPhotos.size}",
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // Info Column
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (selTrip.isUpcoming) UpcomingBadgeColor else VisitedBadgeColor
                                                ) {
                                                    Text(
                                                        text = if (selTrip.isUpcoming) "Upcoming" else "Visited",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    )
                                                }
                                                Text(
                                                    text = if (selSinhalaProv.isNotBlank()) "$selProvince ($selSinhalaProv)" else "$selProvince Prov",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = BentoPrimary
                                                    ),
                                                    maxLines = 1
                                                )
                                            }

                                            Text(
                                                text = selTrip.title.ifBlank { selTrip.locationName },
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = selTrip.locationName,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Close Selection Button
                                        IconButton(
                                            onClick = { viewModel.selectTrip(null) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close preview",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Action Buttons Row: "View Details & Media" & "Edit"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.openTripDetail(selTrip) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoLibrary,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("View Details & Media", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.openEditTripDialog(selTrip) },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, BentoBorderLight)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        // Map Location Picker Confirmation Bottom Banner
                        if (uiState.isMapPickerMode) {
                            val currentCoords = uiState.pickedCoordinates ?: Pair(7.8731, 80.7718)
                            val matchedProv = SriLankaDestinations.findMatchingProvince(currentCoords.first, currentCoords.second)
                            val sinhalaProv = SriLankaDestinations.PROVINCE_SINHALA[matchedProv] ?: ""

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                                    .fillMaxWidth()
                                    .testTag("map_picker_confirmation_card"),
                                shape = RoundedCornerShape(22.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 10.dp,
                                border = BorderStroke(2.dp, BentoPrimary)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(BentoPrimary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddLocation,
                                                    contentDescription = null,
                                                    tint = BentoPrimary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = "Select Location on Map",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "Tap any spot on the map to position pin",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.cancelMapLocationPicker() },
                                            modifier = Modifier.size(32.dp).testTag("btn_cancel_map_picker")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                                        }
                                    }

                                    // Picked Coordinates & Province Badge
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = BentoLavenderContainer.copy(alpha = 0.45f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "📍 ${String.format(java.util.Locale.US, "%.4f", currentCoords.first)}, ${String.format(java.util.Locale.US, "%.4f", currentCoords.second)}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoPrimary
                                                )
                                            )
                                            Text(
                                                text = "$matchedProv ($sinhalaProv)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = BentoPrimaryDark
                                                )
                                            )
                                        }
                                    }

                                    // Confirm and Save Location Action Button
                                    Button(
                                        onClick = { viewModel.confirmMapPickedLocation() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_confirm_save_location"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Confirm & Save Location",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }

                        // Active Journey Route Overlay Banner
                        if (uiState.activeJourneyOnMap != null) {
                            val activeJ = uiState.activeJourneyOnMap!!
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                                    .fillMaxWidth()
                                    .testTag("active_journey_map_banner"),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 8.dp,
                                border = BorderStroke(1.5.dp, BentoPrimary)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Active Journey Route",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = BentoPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = activeJ.trip.tripTitle,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${activeJ.stops.size} Stops • ${activeJ.trip.totalDistanceKm} km",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.clearActiveJourneyOnMap() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear Route")
                                    }
                                }
                            }
                        }

                        // Floating Map Controls (Center Sri Lanka)
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 4.dp,
                                border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.centerMapOnSriLanka() }
                                    .testTag("btn_center_sri_lanka")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CenterFocusStrong,
                                        contentDescription = "Center Sri Lanka",
                                        tint = BentoPrimary
                                    )
                                }
                            }
                        }

                        // Floating Glassmorphic Travel Stats Badge Overlay (card_travel_stats)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                                .testTag("card_travel_stats"),
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xCC121212),
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.5.dp, Color(0x3300E5FF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_ceylonsteps_brand_logo),
                                    contentDescription = "Footsteps",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%,.0f", uiState.stats.totalDistanceKm)} km Explored",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.testTag("tv_traveled_distance")
                                )
                            }
                        }
                    }
                }

                NavigationTab.TIMELINE -> {
                    // Multi-Stop Journeys & Timeline View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .testTag("timeline_journeys_list"),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Multi-Stop Journeys",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Complete itineraries with waypoints & distances",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BentoPrimary,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.openMultiStopBuilder() }
                                        .testTag("btn_new_journey_top")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Journey",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.journeysWithStops.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .testTag("empty_journeys_card"),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.6f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = BentoPrimary.copy(alpha = 0.15f),
                                            modifier = Modifier.size(60.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Route,
                                                    contentDescription = null,
                                                    tint = BentoPrimary,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "No Multi-Stop Journeys Yet",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )

                                        Text(
                                            text = "Plan road trips across Sri Lanka! Model journeys from your Home location to attractions, scenic views, and hotels with automated distances.",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 20.sp
                                            )
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = BentoPrimary,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable { viewModel.openMultiStopBuilder() }
                                                .testTag("btn_empty_plan_journey")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add Journey",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            itemsIndexed(uiState.journeysWithStops, key = { _, j -> j.trip.tripId }) { _, journey ->
                                TripTimelineCard(
                                    tripWithStops = journey,
                                    onViewOnMap = { viewModel.viewJourneyOnMap(it) },
                                    onEditTrip = { viewModel.openMultiStopBuilder(it) },
                                    onDeleteTrip = { viewModel.deleteMultiStopTrip(it) },
                                    onOpenMediaViewer = { uris, idx, title, sub ->
                                        viewModel.openMediaViewer(uris, idx, title, sub)
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }

                NavigationTab.JOURNAL -> {
                    val listState = rememberLazyListState()
                    val totalItems = uiState.filteredTrips.size
                    
                    val scrollProgress by remember {
                        derivedStateOf {
                            if (totalItems <= 1) 1f
                            else {
                                val firstVisible = listState.firstVisibleItemIndex
                                val adjustedIndex = (firstVisible - 2).coerceAtLeast(0)
                                val fraction = (adjustedIndex.toFloat() / (totalItems - 1)).coerceIn(0f, 1f)
                                fraction
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Progressive Map Parallax View
                        OsmMapView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.45f)
                                .align(Alignment.TopCenter)
                                .alpha(0.85f),
                            trips = uiState.filteredTrips,
                            selectedTrip = null,
                            activeJourney = null,
                            centerTarget = null,
                            targetZoom = 7.5,
                            isPickerMode = false,
                            onTripSelected = {},
                            onLocationPicked = {_,_->},
                            scrollProgress = scrollProgress
                        )
                        
                        // Gradient Overlay to blend Map into surface
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.45f)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                            MaterialTheme.colorScheme.background
                                        )
                                    )
                                )
                        )

                        // Bento Modular Feed & Timeline
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .testTag("journal_feed_list"),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                        item {
                            Spacer(modifier = Modifier.height(280.dp))
                            // Bento Modular Dashboard
                            StatsHeaderCard(
                                stats = uiState.stats,
                                latestTrip = uiState.pastTrips.lastOrNull() ?: uiState.trips.firstOrNull(),
                                nextUpcomingTrip = uiState.upcomingTrips.firstOrNull(),
                                onQuickAdd = { viewModel.openAddTripDialog() },
                                onViewMap = { viewModel.setNavigationTab(NavigationTab.MAP) }
                            )
                        }

                        // Filter Chips Row (All, Visited, Upcoming, Province)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = uiState.filterTab == FilterTab.ALL,
                                    onClick = { viewModel.setFilterTab(FilterTab.ALL) },
                                    label = { Text("All (${uiState.trips.size})") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )

                                FilterChip(
                                    selected = uiState.filterTab == FilterTab.VISITED,
                                    onClick = { viewModel.setFilterTab(FilterTab.VISITED) },
                                    label = { Text("Visited (${uiState.pastTrips.size})") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoLavenderContainer,
                                        selectedLabelColor = BentoOnPrimaryContainer
                                    )
                                )

                                FilterChip(
                                    selected = uiState.filterTab == FilterTab.UPCOMING,
                                    onClick = { viewModel.setFilterTab(FilterTab.UPCOMING) },
                                    label = { Text("Upcoming (${uiState.upcomingTrips.size})") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoAmberSecondary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Selected Province Badge (if filtered)
                        if (uiState.selectedProvince != null) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = BentoAmberSecondary.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, BentoAmberSecondary.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Filtering: ${uiState.selectedProvince} Province",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = BentoAmberSecondary
                                            )
                                        )
                                        IconButton(
                                            onClick = { viewModel.setSelectedProvince(null) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear province filter",
                                                tint = BentoAmberSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Empty State for First-Time Users
                        if (uiState.filteredTrips.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .testTag("journal_empty_state"),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.6f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = BentoSkyBlue.copy(alpha = 0.2f),
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = null,
                                                    tint = BentoPrimary,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "No Visited Places Yet",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )

                                        Text(
                                            text = "Your travel log is ready. Tap the '+' button below or select a location directly on the Sri Lanka Map to log places you visit!",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 20.sp
                                            )
                                        )

                                        Button(
                                            onClick = { viewModel.openAddTripDialog() },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                                            modifier = Modifier.testTag("btn_empty_add_trip")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("+ Mark Location", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Trip Cards
                        itemsIndexed(uiState.filteredTrips, key = { _, t -> t.id }) { index, trip ->
                            val nextTrip = uiState.filteredTrips.getOrNull(index + 1)
                            val dist = if (nextTrip != null) {
                                TripRepository.calculateDistanceKm(
                                    trip.latitude,
                                    trip.longitude,
                                    nextTrip.latitude,
                                    nextTrip.longitude
                                )
                            } else null

                            TripCard(
                                trip = trip,
                                index = index + 1,
                                distanceToNextKm = dist,
                                distanceFromHomeKm = GeoDistanceEngine.calculateDistanceFromHomeKm(trip, uiState.userProfile),
                                onClick = { viewModel.openTripDetail(trip) },
                                onMapClick = {
                                    viewModel.selectTrip(trip)
                                    viewModel.setNavigationTab(NavigationTab.MAP)
                                },
                                onEditClick = { viewModel.openEditTripDialog(trip) },
                                onDeleteClick = { viewModel.deleteTrip(trip) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
                }
                NavigationTab.CALENDAR -> {
                    // Month-by-month Travel Calendar
                    CustomCalendarView(
                        trips = uiState.filteredTrips,
                        onTripClick = { viewModel.openTripDetail(it) },
                        onMapClick = {
                            viewModel.selectTrip(it)
                            viewModel.setNavigationTab(NavigationTab.MAP)
                        },
                        onEditClick = { viewModel.openEditTripDialog(it) },
                        onDeleteClick = { viewModel.deleteTrip(it) }
                    )
                }

                NavigationTab.EXPLORER -> {
                    // Curated Sri Lanka Destination Explorer (100+ Destinations)
                    SriLankaExplorerView(
                        existingTrips = uiState.trips,
                        onAddLandmark = { landmark, isUpcoming ->
                            viewModel.openAddTripDialog(preset = landmark)
                        },
                        onViewOnMap = { lat, lng ->
                            viewModel.centerMapOn(lat, lng, 12.5)
                        }
                    )
                }

                NavigationTab.PROFILE -> {
                    // Explorer Profile, Province Badges, Stats & Theme Selector
                    ProfileScreen(
                        userProfile = uiState.userProfile,
                        trips = uiState.trips,
                        totalDistanceKm = uiState.stats.totalDistanceKm,
                        roundTripFromHomeKm = uiState.stats.roundTripFromHomeKm,
                        currentThemeMode = uiState.themeMode,
                        onThemeModeChange = { viewModel.setThemeMode(it) },
                        currentAppThemeType = uiState.appThemeType,
                        onAppThemeTypeChange = { viewModel.setAppThemeType(it) },
                        onEditProfileClick = { viewModel.openEditProfile() },
                        onBackupRestoreClick = { viewModel.openBackupRestoreDialog() },
                        recycledTrips = uiState.recycledTrips,
                        onRestoreTrip = { viewModel.restoreTrip(it) },
                        onPermanentlyDeleteTrip = { viewModel.permanentlyDeleteTrip(it) },
                        onTripClick = { viewModel.openTripDetail(it) }
                    )
                }
            }
        }
    }

    // Modal Multi-Stop Trip Builder & Editor Dialog
    if (uiState.isMultiStopBuilderOpen) {
        AddEditMultiStopTripDialog(
            tripToEdit = uiState.journeyToEdit,
            userProfile = uiState.userProfile,
            onDismissRequest = { viewModel.closeMultiStopBuilder() },
            onSaveTrip = { trip, stops ->
                viewModel.saveMultiStopTrip(trip, stops)
            }
        )
    }

    // Full-Screen Media Viewer Dialog (Photos & Videos)
    if (uiState.mediaViewerState.isOpen) {
        MediaViewerDialog(
            mediaUris = uiState.mediaViewerState.mediaUris,
            initialIndex = uiState.mediaViewerState.initialIndex,
            title = uiState.mediaViewerState.title,
            subtitle = uiState.mediaViewerState.subtitle,
            onDismissRequest = { viewModel.closeMediaViewer() }
        )
    }

    // Offline Map Caching Dialog
    if (uiState.isOfflineMapDialogOpen) {
        OfflineMapCacheDialog(
            mapView = currentMapView,
            onDismissRequest = { viewModel.setOfflineMapDialogOpen(false) }
        )
    }

    // Local Database JSON Backup & Restore Dialog
    if (uiState.isBackupRestoreDialogOpen) {
        BackupRestoreDialog(
            userProfile = uiState.userProfile,
            trips = uiState.trips,
            journeys = uiState.journeysWithStops,
            isLoading = uiState.isBackupLoading,
            lastResult = uiState.backupOperationResult,
            onDismissRequest = { viewModel.closeBackupRestoreDialog() },
            onRestoreBackup = { json, overwrite, restoreProf ->
                viewModel.restoreFromJsonString(json, overwrite, restoreProf)
            },
            onClearResult = { viewModel.clearBackupResult() }
        )
    }

    // Modal Add/Edit Trip BottomSheet
    if (uiState.isAddEditSheetOpen) {
        AddEditTripDialog(
            tripToEdit = uiState.tripToEdit,
            onDismiss = { viewModel.closeAddEditDialog() },
            onPickFromMap = { draft -> viewModel.startMapLocationPicker(draft) },
            onSave = { id, title, desc, lat, lng, loc, date, isUpcoming, photos, cover ->
                viewModel.saveTrip(id, title, desc, lat, lng, loc, date, isUpcoming, photos, cover)
            }
        )
    }

    // Modal Edit Profile BottomSheet
    if (uiState.isEditProfileOpen) {
        EditProfileBottomSheet(
            currentProfile = uiState.userProfile,
            onDismiss = { viewModel.closeEditProfile() },
            onSave = { name, imageUri, homeLocationName, homeLat, homeLng ->
                viewModel.saveUserProfile(name, imageUri, homeLocationName, homeLat, homeLng)
            }
        )
    }

    // Modal Trip Detail BottomSheet
    uiState.detailTrip?.let { detail ->
        TripDetailBottomSheet(
            trip = detail,
            userProfile = uiState.userProfile,
            onDismiss = { viewModel.closeTripDetail() },
            onFocusMap = {
                viewModel.selectTrip(detail)
                viewModel.setNavigationTab(NavigationTab.MAP)
            },
            onEdit = { viewModel.openEditTripDialog(detail) },
            onDelete = { viewModel.deleteTrip(detail) },
            onToggleStatus = { viewModel.toggleTripStatus(detail) },
            onOpenMediaViewer = { uris, index ->
                viewModel.openMediaViewer(uris, index, detail.title, detail.locationName)
            }
        )
    }
}
