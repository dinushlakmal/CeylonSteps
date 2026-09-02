package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.CeylonStepsApp
import com.example.data.model.SriLankaDestinations
import com.example.data.model.SriLankaLandmark
import com.example.data.model.TripLocation
import com.example.data.model.UserProfile
import com.example.data.repository.TripRepository
import com.example.data.repository.UserManager
import com.example.util.GeoDistanceEngine
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.example.util.GoogleDriveBackupEngine
import com.example.util.GoogleDriveMediaEngine
import android.content.Context
import android.widget.Toast
import com.ceylonsteps.travelapp.data.model.Trip
import com.ceylonsteps.travelapp.data.model.TripStop
import com.ceylonsteps.travelapp.data.model.TripWithStops
import com.ceylonsteps.travelapp.data.repository.TripTimelineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    COMMUNITY,
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
    val recycledTrips: List<TripLocation> = emptyList(),
    val journeysWithStops: List<TripWithStops> = emptyList(),
    val activeJourneyOnMap: TripWithStops? = null,
    val isMultiStopBuilderOpen: Boolean = false,
    val journeyToEdit: TripWithStops? = null,
    val mediaViewerState: MediaViewerState = MediaViewerState(),
    val isOfflineMapDialogOpen: Boolean = false,
    val isBackupRestoreDialogOpen: Boolean = false,
    val isBackupLoading: Boolean = false,
    val backupOperationResult: com.example.util.RestoreResult? = null,
    val appThemeType: com.example.data.repository.AppThemeType = com.example.data.repository.AppThemeType.DEFAULT,
    val themeMode: com.example.data.repository.ThemeMode = com.example.data.repository.ThemeMode.LIGHT,
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
    val activeNavigationTab: NavigationTab = NavigationTab.COMMUNITY,
    val isAddEditSheetOpen: Boolean = false,
    val tripToEdit: TripLocation? = null,
    val detailTrip: TripLocation? = null,
    val isMapPickerMode: Boolean = false,
    val pickedCoordinates: Pair<Double, Double>? = null,
    val mapCenterTarget: Pair<Double, Double>? = Pair(7.8731, 80.7718), // Sri Lanka center
    val mapTargetZoom: Double = 8.2,
    val socialPosts: List<com.example.data.model.social.SocialPost> = emptyList(),
    val socialCommentsMap: Map<String, List<com.example.data.model.social.SocialComment>> = emptyMap(),
    val isShareSocialDialogOpen: Boolean = false,
    val isDriveSyncing: Boolean = false,
    val lastDriveSyncTime: Long? = null
)

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TripRepository = (application as CeylonStepsApp).repository
    private val timelineRepository: TripTimelineRepository = (application as CeylonStepsApp).timelineRepository
    private val userManager: UserManager = UserManager.getInstance(application)
    private val socialEngine: com.example.data.repository.FirestoreSocialEngine =
        com.example.data.repository.FirestoreSocialEngine.getInstance(application)

    private val _filterTab = MutableStateFlow(FilterTab.ALL)
    private val _selectedProvince = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _activeNavigationTab = MutableStateFlow(NavigationTab.COMMUNITY)
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
    val recycledTrips: StateFlow<List<TripLocation>> = repository.recycledTrips.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _isBackupLoading = MutableStateFlow(false)
    private val _backupOperationResult = MutableStateFlow<com.example.util.RestoreResult?>(null)
    private val _isShareSocialDialogOpen = MutableStateFlow(false)
    private val _isDriveSyncing = MutableStateFlow(false)
    val isDriveSyncing: StateFlow<Boolean> = _isDriveSyncing
    private val _lastDriveSyncTime = MutableStateFlow<Long?>(null)
    val lastDriveSyncTime: StateFlow<Long?> = _lastDriveSyncTime

    /**
     * Automatically uploads latest database & profile snapshot to Google Drive in background
     */
    fun triggerAutoDriveBackup() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication()) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isDriveSyncing.value = true
                val trips = repository.getAllTripsSync()
                val journeys = timelineRepository.getAllTripsWithStopsSync()
                val success = GoogleDriveBackupEngine.performAutoSync(
                    context = getApplication(),
                    account = account,
                    trips = trips,
                    journeys = journeys
                )
                if (success) {
                    _lastDriveSyncTime.value = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                android.util.Log.w("TripViewModel", "Auto-drive backup error: ${e.message}")
            } finally {
                _isDriveSyncing.value = false
            }
        }
    }

    val uiState: StateFlow<TripUiState> = combine(
        combine(
            repository.allTrips,
            timelineRepository.allTripsWithStops,
            userManager.userProfile,
            _filterTab,
            combine(_selectedProvince, recycledTrips) { p, rt -> Pair(p, rt) }
        ) { trips, journeys, profile, filter, provinceAndRecycled ->
            Triple(trips, journeys, Triple(profile, filter, provinceAndRecycled))
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
            combine(_isOfflineMapDialogOpen, _isBackupRestoreDialogOpen) { off, bak -> Pair(off, bak) },
            combine(_isBackupLoading, _backupOperationResult) { load, res -> Pair(load, res) },
            socialEngine.posts,
            socialEngine.commentsMap,
            _isShareSocialDialogOpen
        ) { dialogs, backupState, posts, commentsMap, isShareOpen ->
            Triple(dialogs, backupState, Triple(posts, commentsMap, isShareOpen))
        }
    ) { group1, group2, group3, group4, group5 ->
        val trips = group1.first
        val journeys = group1.second
        val profile = group1.third.first
        val filter = group1.third.second
        val province = group1.third.third.first
        val recycledList = group1.third.third.second

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

        val isOfflineOpen = group5.first.first
        val isBackupOpen = group5.first.second
        val isBackupLoading = group5.second.first
        val backupResult = group5.second.second
        val socialPosts = group5.third.first
        val socialComments = group5.third.second
        val isShareSocialOpen = group5.third.third

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
            recycledTrips = recycledList,
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
            socialPosts = socialPosts,
            socialCommentsMap = socialComments,
            isShareSocialDialogOpen = isShareSocialOpen,
            themeMode = userManager.getThemeMode(),
            appThemeType = userManager.getAppThemeType()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripUiState(themeMode = userManager.getThemeMode(), appThemeType = userManager.getAppThemeType())
    )

    val appThemeType: StateFlow<com.example.data.repository.AppThemeType> = userManager.appThemeType

    fun setAppThemeType(type: com.example.data.repository.AppThemeType) {
        userManager.setAppThemeType(type)
    }

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
            triggerAutoDriveBackup()
        }
    }

    fun deleteMultiStopTrip(tripId: Long) {
        viewModelScope.launch {
            timelineRepository.deleteTrip(tripId)
            if (_activeJourneyOnMap.value?.trip?.tripId == tripId) {
                _activeJourneyOnMap.value = null
            }
            triggerAutoDriveBackup()
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
            triggerAutoDriveBackup()
        }
    }


    init {
        viewModelScope.launch {
            repository.cleanOldRecycledTrips()
        }
    }

    fun deleteTrip(trip: TripLocation) {
        viewModelScope.launch {
            repository.updateTrip(trip.copy(deletedAtEpochMillis = System.currentTimeMillis()))
            if (_selectedTrip.value?.id == trip.id) {
                _selectedTrip.value = null
            }
            if (_detailTrip.value?.id == trip.id) {
                _detailTrip.value = null
            }
            triggerAutoDriveBackup()
        }
    }

    fun restoreTrip(trip: TripLocation) {
        viewModelScope.launch {
            repository.updateTrip(trip.copy(deletedAtEpochMillis = null))
            triggerAutoDriveBackup()
        }
    }

    fun permanentlyDeleteTrip(trip: TripLocation) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
            triggerAutoDriveBackup()
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
            triggerAutoDriveBackup()
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

    fun restoreFromGoogleDriveAuto(
        context: Context,
        account: GoogleSignInAccount,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            try {
                val jsonString = GoogleDriveBackupEngine.restoreFromDrive(context, account) { _, _ -> }
                if (!jsonString.isNullOrBlank()) {
                    val database = com.example.data.db.AppDatabase.getDatabase(getApplication())
                    val parsedBackup = com.example.util.DatabaseBackupManager.parseJson(jsonString)
                    val result = com.example.util.DatabaseBackupManager.restoreDatabase(
                        context = context,
                        database = database,
                        backupData = parsedBackup,
                        overwriteExisting = false,
                        restoreUserProfile = true
                    )
                    _backupOperationResult.value = result
                    onComplete?.invoke(true, "Cloud backup restored: ${result.tripsImported} footprints & journeys synchronized!")
                } else {
                    onComplete?.invoke(false, "No previous backup found on Google Drive.")
                }
            } catch (e: Exception) {
                onComplete?.invoke(false, "Restore error: ${e.localizedMessage ?: "Unknown error"}")
            } finally {
                _isBackupLoading.value = false
            }
        }
    }

    fun saveUserProfile(
        name: String,
        imageUri: String?,
        coverUri: String? = null,
        bio: String = "",
        homeLocationName: String,
        homeLat: Double,
        homeLng: Double
    ) {
        userManager.updateProfile(
            name = name,
            imageUri = imageUri,
            coverUri = coverUri,
            bio = bio,
            homeLocationName = homeLocationName,
            homeLat = homeLat,
            homeLng = homeLng
        )
        _isEditProfileOpen.value = false

        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        if (account != null) {
            viewModelScope.launch(Dispatchers.IO) {
                var finalCover = coverUri
                var finalAvatar = imageUri
                if (coverUri != null && !coverUri.startsWith("http://") && !coverUri.startsWith("https://")) {
                    val cdn = GoogleDriveMediaEngine.uploadSingleImage(getApplication(), account, coverUri, "ceylon_cover")
                    if (cdn != null) finalCover = cdn
                }
                if (imageUri != null && !imageUri.startsWith("http://") && !imageUri.startsWith("https://")) {
                    val cdn = GoogleDriveMediaEngine.uploadSingleImage(getApplication(), account, imageUri, "ceylon_avatar")
                    if (cdn != null) finalAvatar = cdn
                }
                userManager.updateProfile(
                    name = name,
                    imageUri = finalAvatar,
                    coverUri = finalCover,
                    bio = bio,
                    homeLocationName = homeLocationName,
                    homeLat = homeLat,
                    homeLng = homeLng
                )
                triggerAutoDriveBackup()
            }
        } else {
            triggerAutoDriveBackup()
        }
    }

    // ==========================================
    // SOCIAL FEED & COMMUNITY ENGINE
    // ==========================================

    fun setShareSocialDialogOpen(isOpen: Boolean) {
        _isShareSocialDialogOpen.value = isOpen
    }

    fun togglePostLike(postId: String) {
        viewModelScope.launch {
            val currentUserId = userManager.userProfile.value.userName
            socialEngine.toggleLike(postId, currentUserId)
        }
    }

    fun toggleUserFollow(targetUserId: String) {
        viewModelScope.launch {
            val currentUserId = userManager.userProfile.value.userName
            socialEngine.toggleFollow(targetUserId, currentUserId)
        }
    }

    fun addSocialComment(postId: String, text: String) {
        viewModelScope.launch {
            val profile = userManager.userProfile.value
            val comment = com.example.data.model.social.SocialComment(
                postId = postId,
                authorId = profile.userName,
                authorName = profile.userName.ifBlank { "Explorer" },
                authorAvatarUrl = profile.profileImageUri,
                authorBadge = "Lanka Explorer",
                text = text
            )
            socialEngine.addComment(postId, comment)
        }
    }

    fun listenToSocialComments(postId: String) {
        socialEngine.listenToComments(postId)
    }

    fun deleteSocialPost(postId: String) {
        viewModelScope.launch {
            val profile = userManager.userProfile.value
            val authorId = profile.userEmail.ifBlank { profile.userName.ifBlank { "user_local_explorer" } }
            socialEngine.deletePost(postId, authorId)
        }
    }

    fun updateSocialPost(post: com.example.data.model.social.SocialPost) {
        viewModelScope.launch {
            socialEngine.updatePost(post)
        }
    }

    fun saveCommunityPostToLocalTrips(post: com.example.data.model.social.SocialPost) {
        viewModelScope.launch {
            val trip = TripLocation(
                title = post.title,
                locationName = post.locationName,
                latitude = post.latitude,
                longitude = post.longitude,
                dateEpochMillis = post.travelDateEpoch,
                description = "${post.story}\n\nShared by ${post.authorName} on CeylonSteps Community." +
                        if (post.isMultiStop) "\nStops: ${post.stopNames.joinToString(" ➔ ")}" else "",
                isUpcoming = false,
                imageUrisJson = TripRepository.toJsonArray(post.mediaUrls),
                coverImageUri = post.mediaUrls.firstOrNull()
            )
            repository.insertTrip(trip)
            triggerAutoDriveBackup()
        }
    }

    val isUserLoggedIn: StateFlow<Boolean> = userManager.isLoggedIn

    val followingUserIds: StateFlow<Set<String>> = socialEngine.followingUserIds
    val followersList: StateFlow<List<com.example.data.model.social.FollowerUser>> = socialEngine.followersList
    val followingList: StateFlow<List<com.example.data.model.social.FollowerUser>> = socialEngine.followingList

    fun removeFollower(targetUserId: String) {
        socialEngine.removeFollower(targetUserId)
    }

    fun updateProfileAvatar(imageUri: String?) {
        userManager.updateAvatar(imageUri)
        if (imageUri != null && !imageUri.startsWith("http://") && !imageUri.startsWith("https://")) {
            val account = GoogleSignIn.getLastSignedInAccount(getApplication())
            if (account != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        _isDriveSyncing.value = true
                        val cdnUrl = GoogleDriveMediaEngine.uploadSingleImage(
                            context = getApplication(),
                            account = account,
                            imageUri = imageUri,
                            filePrefix = "ceylon_avatar"
                        )
                        if (cdnUrl != null) {
                            userManager.updateAvatar(cdnUrl)
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("TripViewModel", "Avatar upload to Drive failed: ${e.message}")
                    } finally {
                        _isDriveSyncing.value = false
                        triggerAutoDriveBackup()
                    }
                }
                return
            }
        }
        triggerAutoDriveBackup()
    }

    fun updateProfileCover(coverUri: String?) {
        userManager.updateCover(coverUri)
        if (coverUri != null && !coverUri.startsWith("http://") && !coverUri.startsWith("https://")) {
            val account = GoogleSignIn.getLastSignedInAccount(getApplication())
            if (account != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        _isDriveSyncing.value = true
                        val cdnUrl = GoogleDriveMediaEngine.uploadSingleImage(
                            context = getApplication(),
                            account = account,
                            imageUri = coverUri,
                            filePrefix = "ceylon_cover"
                        )
                        if (cdnUrl != null) {
                            userManager.updateCover(cdnUrl)
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("TripViewModel", "Cover upload to Drive failed: ${e.message}")
                    } finally {
                        _isDriveSyncing.value = false
                        triggerAutoDriveBackup()
                    }
                }
                return
            }
        }
        triggerAutoDriveBackup()
    }

    fun loginWithGoogle(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount) {
        val email = account.email ?: "explorer@ceylonsteps.lk"
        val name = account.displayName ?: email.substringBefore("@")
        val photo = account.photoUrl?.toString()
        userManager.loginWithGoogle(email, name, photo)
        com.ceylonsteps.travelapp.auth.UserManager.autoRegisterFromGoogle(
            context = getApplication(),
            account = account
        )

        // Automatically sync and restore past data from Google Drive on login
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isDriveSyncing.value = true
                val database = (getApplication() as CeylonStepsApp).database
                val restoreResult = GoogleDriveBackupEngine.restoreAndApplyFromDrive(
                    context = getApplication(),
                    account = account,
                    database = database
                )
                withContext(Dispatchers.Main) {
                    if (restoreResult.isSuccess && (restoreResult.tripsImported > 0 || restoreResult.journeysImported > 0)) {
                        Toast.makeText(
                            getApplication(),
                            "Google Drive data restored: ${restoreResult.tripsImported} footprints & profile! 🇱🇰",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("TripViewModel", "Auto-restore on login failed: ${e.message}")
            } finally {
                _isDriveSyncing.value = false
                triggerAutoDriveBackup()
            }
        }
    }

    fun loginWithEmail(email: String, pass: String): Result<UserProfile> {
        return userManager.loginWithEmail(email, pass)
    }

    fun registerWithEmail(email: String, pass: String, name: String): Result<UserProfile> {
        return userManager.registerWithEmail(email, pass, name)
    }

    fun loginAsGuest(): UserProfile {
        return userManager.loginAsGuest()
    }

    fun logoutUser(context: Context) {
        userManager.logout()
        com.ceylonsteps.travelapp.auth.UserManager.signOut(context)
    }
}

