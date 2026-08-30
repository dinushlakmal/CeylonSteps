package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LankaFootprintsApp
import com.example.data.model.SriLankaDestinations
import com.example.data.model.SriLankaLandmark
import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import com.example.data.repository.TripRepository
import com.example.data.repository.UserManager
import com.example.util.GeoDistanceEngine
import com.lankafootprints.travelapp.data.model.Trip
import com.lankafootprints.travelapp.data.model.TripStop
import com.lankafootprints.travelapp.data.model.TripWithStops
import com.lankafootprints.travelapp.data.repository.TripTimelineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterTab {
    ALL,
    VISITED,
    UPCOMING
}

enum class NavigationTab {
    MAP,
    JOURNAL,
    TIMELINE,
    CALENDAR,
    EXPLORER,
    PROFILE
}

data class TripStats(
    val totalDistanceKm: Double = 0.0,
    val roundTripFromHomeKm: Double = 0.0,
    val visitedCount: Int = 0,
    val upcomingCount: Int = 0,
    val totalFootprints: Int = 0,
    val uniqueProvinces: Int = 0
)

data class MediaViewerState(
    val isOpen: Boolean = false,
    val mediaUris: List<String> = emptyList(),
    val initialIndex: Int = 0,
    val title: String = "",
    val subtitle: String? = null
)

data class TripUiState(
    val trips: List<TripLocation> = emptyList(),
    val journeysWithStops: List<TripWithStops> = emptyList(),
    val activeJourneyOnMap: TripWithStops? = null,
    val isMultiStopBuilderOpen: Boolean = false,
    val journeyToEdit: TripWithStops? = null,
    val mediaViewerState: MediaViewerState = MediaViewerState(),
    val isOfflineMapDialogOpen: Boolean = false,
    val isBackupRestoreDialogOpen: Boolean = false,
    val isBackupLoading: Boolean = false,
    val backupOperationResult: com.example.util.RestoreResult? = null,
    val themeMode: com.example.data.repository.ThemeMode = com.example.data.repository.ThemeMode.SYSTEM,
    val filteredTrips: List<TripLocation> = emptyList(),
    val pastTrips: List<TripLocation> = emptyList(),
    val upcomingTrips: List<TripLocation> = emptyList(),
    val stats: TripStats = TripStats(),
    val userProfile: UserProfile = UserProfile(),
    val isEditProfileOpen: Boolean = false,
    val selectedTrip: TripLocation? = null,
    val filterTab: FilterTab = FilterTab.ALL,
    val selectedProvince: String? = null,
    val searchQuery: String = "",
    val activeNavigationTab: NavigationTab = NavigationTab.MAP,
    val isAddEditSheetOpen: Boolean = false,
    val tripToEdit: TripLocation? = null,
    val detailTrip: TripLocation? = null,
    val isMapPickerMode: Boolean = false,
    val pickedCoordinates: Pair<Double, Double>? = null,
    val mapCenterTarget: Pair<Double, Double>? = Pair(7.8731, 80.7718), // Sri Lanka center
    val mapTargetZoom: Double = 8.2
)

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TripRepository = (application as LankaFootprintsApp).repository
    private val timelineRepository: TripTimelineRepository = (application as LankaFootprintsApp).timelineRepository
    private val userManager: UserManager = UserManager.getInstance(application)

    private val _filterTab = MutableStateFlow(FilterTab.ALL)
    private val _selectedProvince = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _activeNavigationTab = MutableStateFlow(NavigationTab.MAP)
    private val _selectedTrip = MutableStateFlow<TripLocation?>(null)
    private val _isAddEditSheetOpen = MutableStateFlow(false)
    private val _isEditProfileOpen = MutableStateFlow(false)
    private val _tripToEdit = MutableStateFlow<TripLocation?>(null)
    private val _detailTrip = MutableStateFlow<TripLocation?>(null)
    private val _isMapPickerMode = MutableStateFlow(false)
    private val _pickedCoordinates = MutableStateFlow<Pair<Double, Double>?>(null)
    private val _mapCenterTarget = MutableStateFlow<Pair<Double, Double>?>(Pair(7.8731, 80.7718))
    private val _mapTargetZoom = MutableStateFlow(8.2)

    // Multi-Stop Timeline & Media State
    private val _isMultiStopBuilderOpen = MutableStateFlow(false)
    private val _journeyToEdit = MutableStateFlow<TripWithStops?>(null)
    private val _activeJourneyOnMap = MutableStateFlow<TripWithStops?>(null)
    private val _mediaViewerState = MutableStateFlow(MediaViewerState())
    private val _isOfflineMapDialogOpen = MutableStateFlow(false)
    private val _isBackupRestoreDialogOpen = MutableStateFlow(false)
    private val _isBackupLoading = MutableStateFlow(false)
    private val _backupOperationResult = MutableStateFlow<com.example.util.RestoreResult?>(null)

    val uiState: StateFlow<TripUiState> = combine(
        combine(
            repository.allTrips,
            timelineRepository.allTripsWithStops,
            userManager.userProfile,
            _filterTab,
            _selectedProvince
        ) { trips, journeys, profile, filter, province ->
            Triple(trips, journeys, Triple(profile, filter, province))
        },
        combine(
            _searchQuery,
            _activeNavigationTab,
            _selectedTrip,
            _isAddEditSheetOpen,
            _isEditProfileOpen
        ) { query, navTab, selected, isSheetOpen, isEditProf ->
            Triple(query, navTab, Triple(selected, isSheetOpen, isEditProf))
        },
        combine(
            _tripToEdit,
            _detailTrip,
            _isMapPickerMode,
            _pickedCoordinates,
            _mapCenterTarget
        ) { editTrip, detail, isPicker, pickedCoords, center ->
            Triple(editTrip, detail, Triple(isPicker, pickedCoords, center))
        },
        combine(
            _mapTargetZoom,
            _isMultiStopBuilderOpen,
            _journeyToEdit,
            _activeJourneyOnMap,
            _mediaViewerState
        ) { zoom, isBuilderOpen, editJourney, activeJourney, mediaState ->
            Triple(zoom, isBuilderOpen, Triple(editJourney, activeJourney, mediaState))
        },
        combine(
            _isOfflineMapDialogOpen,
            _isBackupRestoreDialogOpen,
            _isBackupLoading,
            _backupOperationResult
        ) { isOffline, isBackup, isLoading, result ->
            Triple(isOffline, isBackup, Pair(isLoading, result))
        }
    ) { group1, group2, group3, group4, group5 ->
        val trips = group1.first
        val journeys = group1.second
        val profile = group1.third.first
        val filter = group1.third.second
        val province = group1.third.third

        val query = group2.first
        val navTab = group2.second
        val selected = group2.third.first
        val isSheetOpen = group2.third.second
        val isEditProf = group2.third.third

        val editTrip = group3.first
        val detail = group3.second
        val isPicker = group3.third.first
        val pickedCoords = group3.third.second
        val center = group3.third.third

        val zoom = group4.first
        val isBuilderOpen = group4.second
        val editJourney = group4.third.first
        val activeJourney = group4.third.second
        val mediaState = group4.third.third

        val isOfflineOpen = group5.first
        val isBackupOpen = group5.second
        val isBackupLoading = group5.third.first
        val backupResult = group5.third.second

        val past = trips.filter { !it.isUpcoming }.sortedBy { it.dateEpochMillis }
        val upcoming = trips.filter { it.isUpcoming }.sortedBy { it.dateEpochMillis }

        val filtered = trips.filter { trip ->
            val matchesFilter = when (filter) {
                FilterTab.ALL -> true
                FilterTab.VISITED -> !trip.isUpcoming
                FilterTab.UPCOMING -> trip.isUpcoming
            }
            val matchesProvince = province == null ||
                    SriLankaDestinations.findMatchingProvince(trip.latitude, trip.longitude).equals(province, ignoreCase = true) ||
                    trip.locationName.contains(province, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    trip.title.contains(query, ignoreCase = true) ||
                    trip.locationName.contains(query, ignoreCase = true) ||
                    trip.description.contains(query, ignoreCase = true)

            matchesFilter && matchesProvince && matchesQuery
        }.sortedBy { it.dateEpochMillis }

        val totalRoute = TripRepository.calculateTotalRouteDistance(past)
        val roundTripKm = GeoDistanceEngine.calculateRoundTripDistanceKm(past, profile)

        val stats = TripStats(
            totalDistanceKm = totalRoute,
            roundTripFromHomeKm = roundTripKm,
            visitedCount = past.size,
            upcomingCount = upcoming.size,
            totalFootprints = trips.size,
            uniqueProvinces = TripRepository.getUniqueProvincesCount(past)
        )

        TripUiState(
            trips = trips,
            journeysWithStops = journeys,
            activeJourneyOnMap = activeJourney,
            isMultiStopBuilderOpen = isBuilderOpen,
            journeyToEdit = editJourney,
            mediaViewerState = mediaState,
            isOfflineMapDialogOpen = isOfflineOpen,
            isBackupRestoreDialogOpen = isBackupOpen,
            isBackupLoading = isBackupLoading,
            backupOperationResult = backupResult,
            filteredTrips = filtered,
            pastTrips = past,
            upcomingTrips = upcoming,
            stats = stats,
            userProfile = profile,
            isEditProfileOpen = isEditProf,
            selectedTrip = selected,
            filterTab = filter,
            selectedProvince = province,
            searchQuery = query,
            activeNavigationTab = navTab,
            isAddEditSheetOpen = isSheetOpen,
            tripToEdit = editTrip,
            detailTrip = detail,
            isMapPickerMode = isPicker,
            pickedCoordinates = pickedCoords,
            mapCenterTarget = center,
            mapTargetZoom = zoom,
            themeMode = userManager.getThemeMode()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripUiState(themeMode = userManager.getThemeMode())
    )

    val themeMode: StateFlow<com.example.data.repository.ThemeMode> = userManager.themeMode

    fun setThemeMode(mode: com.example.data.repository.ThemeMode) {
        userManager.setThemeMode(mode)
    }

    fun openMultiStopBuilder(journey: TripWithStops? = null) {
        _journeyToEdit.value = journey
        _isMultiStopBuilderOpen.value = true
    }

    fun closeMultiStopBuilder() {
        _isMultiStopBuilderOpen.value = false
        _journeyToEdit.value = null
    }

    fun saveMultiStopTrip(trip: Trip, stops: List<TripStop>) {
        viewModelScope.launch {
            timelineRepository.saveMultiStopTrip(trip, stops)
            _isMultiStopBuilderOpen.value = false
            _journeyToEdit.value = null
        }
    }

    fun deleteMultiStopTrip(tripId: Long) {
        viewModelScope.launch {
            timelineRepository.deleteTrip(tripId)
            if (_activeJourneyOnMap.value?.trip?.tripId == tripId) {
                _activeJourneyOnMap.value = null
            }
        }
    }

    fun viewJourneyOnMap(journey: TripWithStops) {
        _activeJourneyOnMap.value = journey
        _activeNavigationTab.value = NavigationTab.MAP
        _mapCenterTarget.value = Pair(journey.trip.originLatitude, journey.trip.originLongitude)
        _mapTargetZoom.value = 10.0
    }

    fun clearActiveJourneyOnMap() {
        _activeJourneyOnMap.value = null
    }

    fun openMediaViewer(mediaUris: List<String>, index: Int = 0, title: String = "", subtitle: String? = null) {
        _mediaViewerState.value = MediaViewerState(
            isOpen = true,
            mediaUris = mediaUris,
            initialIndex = index,
            title = title,
            subtitle = subtitle
        )
    }

    fun closeMediaViewer() {
        _mediaViewerState.value = MediaViewerState(isOpen = false)
    }

    fun setOfflineMapDialogOpen(isOpen: Boolean) {
        _isOfflineMapDialogOpen.value = isOpen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterTab(tab: FilterTab) {
        _filterTab.value = tab
    }

    fun setSelectedProvince(province: String?) {
        _selectedProvince.value = if (_selectedProvince.value == province) null else province
    }

    fun setNavigationTab(tab: NavigationTab) {
        _activeNavigationTab.value = tab
    }

    fun selectTrip(trip: TripLocation?) {
        _selectedTrip.value = trip
        if (trip != null) {
            _mapCenterTarget.value = Pair(trip.latitude, trip.longitude)
            _mapTargetZoom.value = 13.0
        }
    }

    fun centerMapOnSriLanka() {
        _selectedTrip.value = null
        _mapCenterTarget.value = Pair(7.8731, 80.7718)
        _mapTargetZoom.value = 8.2
    }

    fun openAddTripDialog(preset: SriLankaLandmark? = null, initialLat: Double? = null, initialLng: Double? = null) {
        _tripToEdit.value = if (preset != null) {
            TripLocation(
                id = 0,
                title = preset.title,
                description = preset.description,
                latitude = preset.latitude,
                longitude = preset.longitude,
                locationName = preset.locationName,
                dateEpochMillis = System.currentTimeMillis(),
                isUpcoming = true,
                imageUrisJson = TripRepository.toJsonArray(listOf(preset.defaultCoverUri)),
                coverImageUri = preset.defaultCoverUri
            )
        } else if (initialLat != null && initialLng != null) {
            val prov = SriLankaDestinations.findMatchingProvince(initialLat, initialLng)
            TripLocation(
                id = 0,
                title = "",
                description = "",
                latitude = initialLat,
                longitude = initialLng,
                locationName = "$prov Province, Sri Lanka",
                dateEpochMillis = System.currentTimeMillis(),
                isUpcoming = false,
                imageUrisJson = "[]",
                coverImageUri = null
            )
        } else {
            null
        }
        _isAddEditSheetOpen.value = true
    }

    fun openEditTripDialog(trip: TripLocation) {
        _tripToEdit.value = trip
        _isAddEditSheetOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditSheetOpen.value = false
        _tripToEdit.value = null
    }

    fun openTripDetail(trip: TripLocation) {
        _detailTrip.value = trip
        _selectedTrip.value = trip
    }

    fun closeTripDetail() {
        _detailTrip.value = null
    }

    fun saveTrip(
        id: Long = 0,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        locationName: String,
        dateEpochMillis: Long,
        isUpcoming: Boolean,
        imageUris: List<String>,
        coverImageUri: String?
    ) {
        viewModelScope.launch {
            val jsonPhotos = TripRepository.toJsonArray(imageUris)
            val effectiveCover = coverImageUri ?: imageUris.firstOrNull()

            val trip = TripLocation(
                id = id,
                title = title.ifBlank { locationName },
                description = description,
                latitude = latitude,
                longitude = longitude,
                locationName = locationName.ifBlank { "Sri Lanka" },
                dateEpochMillis = dateEpochMillis,
                isUpcoming = isUpcoming,
                imageUrisJson = jsonPhotos,
                coverImageUri = effectiveCover
            )

            if (id == 0L) {
                val newId = repository.insertTrip(trip)
                _selectedTrip.value = trip.copy(id = newId)
            } else {
                repository.updateTrip(trip)
                _selectedTrip.value = trip
                if (_detailTrip.value?.id == id) {
                    _detailTrip.value = trip
                }
            }

            _mapCenterTarget.value = Pair(latitude, longitude)
            _mapTargetZoom.value = 12.5
            closeAddEditDialog()
        }
    }

    fun deleteTrip(trip: TripLocation) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
            if (_selectedTrip.value?.id == trip.id) {
                _selectedTrip.value = null
            }
            if (_detailTrip.value?.id == trip.id) {
                _detailTrip.value = null
            }
        }
    }

    fun toggleTripStatus(trip: TripLocation) {
        viewModelScope.launch {
            val updated = trip.copy(isUpcoming = !trip.isUpcoming)
            repository.updateTrip(updated)
            if (_selectedTrip.value?.id == trip.id) {
                _selectedTrip.value = updated
            }
            if (_detailTrip.value?.id == trip.id) {
                _detailTrip.value = updated
            }
        }
    }

    fun startMapLocationPicker(draftTrip: TripLocation? = null) {
        if (draftTrip != null) {
            _tripToEdit.value = draftTrip
            _pickedCoordinates.value = Pair(draftTrip.latitude, draftTrip.longitude)
            _mapCenterTarget.value = Pair(draftTrip.latitude, draftTrip.longitude)
            _mapTargetZoom.value = 11.5
        } else {
            _pickedCoordinates.value = Pair(7.8731, 80.7718)
        }
        _isMapPickerMode.value = true
        _isAddEditSheetOpen.value = false
        _activeNavigationTab.value = NavigationTab.MAP
    }

    fun onMapLocationPicked(lat: Double, lng: Double) {
        _pickedCoordinates.value = Pair(lat, lng)
        _mapCenterTarget.value = Pair(lat, lng)
        // Keep picker mode open so user can see their pin and tap Confirm & Save
    }

    fun confirmMapPickedLocation() {
        val coords = _pickedCoordinates.value ?: Pair(7.8731, 80.7718)
        val prov = SriLankaDestinations.findMatchingProvince(coords.first, coords.second)
        val currentDraft = _tripToEdit.value
        val updatedDraft = if (currentDraft != null) {
            currentDraft.copy(
                latitude = coords.first,
                longitude = coords.second,
                locationName = if (currentDraft.locationName.isBlank() || currentDraft.locationName.contains("Province")) {
                    "$prov Province, Sri Lanka"
                } else currentDraft.locationName
            )
        } else {
            TripLocation(
                id = 0,
                title = "",
                description = "",
                latitude = coords.first,
                longitude = coords.second,
                locationName = "$prov Province, Sri Lanka",
                dateEpochMillis = System.currentTimeMillis(),
                isUpcoming = false,
                imageUrisJson = "[]",
                coverImageUri = null
            )
        }
        _tripToEdit.value = updatedDraft
        _isMapPickerMode.value = false
        _isAddEditSheetOpen.value = true
    }

    fun cancelMapLocationPicker() {
        _isMapPickerMode.value = false
        _isAddEditSheetOpen.value = true
    }

    fun centerMapOn(lat: Double, lng: Double, zoom: Double = 12.0) {
        _mapCenterTarget.value = Pair(lat, lng)
        _mapTargetZoom.value = zoom
        _activeNavigationTab.value = NavigationTab.MAP
    }

    fun openEditProfile() {
        _isEditProfileOpen.value = true
    }

    fun closeEditProfile() {
        _isEditProfileOpen.value = false
    }

    fun openBackupRestoreDialog() {
        _backupOperationResult.value = null
        _isBackupRestoreDialogOpen.value = true
    }

    fun closeBackupRestoreDialog() {
        _isBackupRestoreDialogOpen.value = false
        _backupOperationResult.value = null
    }

    fun clearBackupResult() {
        _backupOperationResult.value = null
    }

    suspend fun generateBackupJson(): String {
        val userProfile = userManager.userProfile.value
        val trips = repository.getAllTripsSync()
        val journeys = timelineRepository.getAllTripsWithStopsSync()
        return com.example.util.DatabaseBackupManager.exportToJson(
            userProfile = userProfile,
            tripLocations = trips,
            multiStopJourneys = journeys
        )
    }

    fun restoreFromJsonString(
        jsonString: String,
        overwriteExisting: Boolean,
        restoreUserProfile: Boolean
    ) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            val database = com.example.data.db.AppDatabase.getDatabase(getApplication())
            try {
                val parsedBackup = com.example.util.DatabaseBackupManager.parseJson(jsonString)
                val result = com.example.util.DatabaseBackupManager.restoreDatabase(
                    context = getApplication(),
                    database = database,
                    backupData = parsedBackup,
                    overwriteExisting = overwriteExisting,
                    restoreUserProfile = restoreUserProfile
                )
                _backupOperationResult.value = result
            } catch (e: Exception) {
                _backupOperationResult.value = com.example.util.RestoreResult(
                    isSuccess = false,
                    tripsImported = 0,
                    journeysImported = 0,
                    stopsImported = 0,
                    message = "Invalid backup file: ${e.localizedMessage ?: "Parsing error"}"
                )
            } finally {
                _isBackupLoading.value = false
            }
        }
    }

    fun saveUserProfile(
        name: String,
        imageUri: String?,
        homeLocationName: String,
        homeLat: Double,
        homeLng: Double
    ) {
        userManager.updateProfile(
            name = name,
            imageUri = imageUri,
            homeLocationName = homeLocationName,
            homeLat = homeLat,
            homeLng = homeLng
        )
        _isEditProfileOpen.value = false
    }
}

