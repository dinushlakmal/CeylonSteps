package com.example.ui.components

import android.content.Context
import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.TripLocation
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    trips: List<TripLocation>,
    selectedTrip: TripLocation?,
    activeJourney: com.lankafootprints.travelapp.data.model.TripWithStops? = null,
    centerTarget: Pair<Double, Double>?,
    targetZoom: Double,
    isPickerMode: Boolean,
    pickedCoordinates: Pair<Double, Double>? = null,
    homeLocation: Pair<Double, Double>? = null,
    onTripSelected: (TripLocation) -> Unit,
    onLocationPicked: (Double, Double) -> Unit,
    useTopoMap: Boolean = false,
    onMapViewReady: ((MapView) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = modifier.fillMaxSize().testTag("osm_map_container")) {
        AndroidView(
            modifier = Modifier.fillMaxSize().testTag("osm_map_view"),
            factory = { ctx ->
                MapView(ctx).apply {
                    // Set Tile Source: TileSourceFactory.MAPNIK (Free OpenStreetMap)
                    setTileSource(if (useTopoMap) TileSourceFactory.OpenTopo else TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    isTilesScaledToDpi = true

                    // Constrain Map strictly to Sri Lanka Bounding Box
                    val sriLankaBounds = BoundingBox(9.90, 81.90, 5.85, 79.60)
                    setScrollableAreaLimitDouble(sriLankaBounds)
                    minZoomLevel = 7.5
                    maxZoomLevel = 18.0
                    controller.setCenter(GeoPoint(7.8731, 80.7718)) // Center of Sri Lanka
                    controller.setZoom(8.2)

                    mapViewInstance = this
                    onMapViewReady?.invoke(this)
                }
            },
            update = { mapView ->
                mapView.setTileSource(if (useTopoMap) TileSourceFactory.OpenTopo else TileSourceFactory.MAPNIK)

                // Constrain Map strictly to Sri Lanka Bounding Box
                val sriLankaBounds = BoundingBox(9.90, 81.90, 5.85, 79.60)
                mapView.setScrollableAreaLimitDouble(sriLankaBounds)
                mapView.minZoomLevel = 7.5
                mapView.maxZoomLevel = 18.0

                // Clear previous custom overlays
                mapView.overlays.clear()

                // 1. Add Map Events Receiver for Taps / Location Picking
                val eventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        if (p != null) {
                            if (isPickerMode) {
                                onLocationPicked(p.latitude, p.longitude)
                                return true
                            }
                        }
                        return false
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        if (p != null) {
                            onLocationPicked(p.latitude, p.longitude)
                            return true
                        }
                        return false
                    }
                }
                mapView.overlays.add(MapEventsOverlay(eventsReceiver))

                // 2. Render Active Multi-Stop Journey Route if set
                if (activeJourney != null) {
                    val journeyPoints = mutableListOf<GeoPoint>()
                    journeyPoints.add(GeoPoint(activeJourney.trip.originLatitude, activeJourney.trip.originLongitude))
                    activeJourney.stops.sortedBy { it.stopOrder }.forEach { st ->
                        journeyPoints.add(GeoPoint(st.latitude, st.longitude))
                    }

                    if (journeyPoints.size >= 2) {
                        val journeyTrail = AnimatedPolylineOverlay(
                            mapView = mapView,
                            glowColorHex = "#667C4DFF", // Vibrant Purple Glow
                            coreColorHex = "#7C4DFF",  // Purple Core
                            isDashed = false
                        )
                        journeyTrail.setRoutePoints(journeyPoints)
                        mapView.overlays.add(journeyTrail)
                    }

                    // Origin Pin
                    val originMarker = Marker(mapView).apply {
                        position = GeoPoint(activeJourney.trip.originLatitude, activeJourney.trip.originLongitude)
                        title = activeJourney.trip.originName
                        snippet = "Start Point @ ${activeJourney.trip.departureTime}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = CustomMarkerRenderer.createStopPin(
                            context = context,
                            stopType = com.lankafootprints.travelapp.data.model.StopType.START_POINT,
                            order = 0
                        )
                    }
                    mapView.overlays.add(originMarker)

                    // Waypoint Pins
                    activeJourney.stops.sortedBy { it.stopOrder }.forEachIndexed { idx, stop ->
                        val stopMarker = Marker(mapView).apply {
                            position = GeoPoint(stop.latitude, stop.longitude)
                            title = stop.stopName
                            snippet = "${stop.stopType.displayName}\nArr: ${stop.arrivalTime}${if (!stop.departureTime.isNullOrBlank()) " • Dep: ${stop.departureTime}" else ""}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = CustomMarkerRenderer.createStopPin(
                                context = context,
                                stopType = stop.stopType,
                                order = idx + 1
                            )
                        }
                        mapView.overlays.add(stopMarker)
                    }
                } else {
                    // Standard Past/Upcoming Footprints Trail
                    val pastTrips = trips.filter { !it.isUpcoming }.sortedBy { it.dateEpochMillis }
                    val upcomingTrips = trips.filter { it.isUpcoming }.sortedBy { it.dateEpochMillis }

                    // Past Visited Route (Electric Cyan Glow Animated Trail)
                    if (pastTrips.size >= 2) {
                        val points = pastTrips.map { GeoPoint(it.latitude, it.longitude) }
                        val animatedTrail = AnimatedPolylineOverlay(
                            mapView = mapView,
                            glowColorHex = "#4D00E5FF", // Translucent Electric Cyan Glow
                            coreColorHex = "#00E5FF",  // Electric Cyan Core
                            isDashed = true
                        )
                        animatedTrail.setRoutePoints(points)
                        mapView.overlays.add(animatedTrail)
                    }

                    // Upcoming Route Polyline (Gold Trail Connected from last spot)
                    val connectionPoints = mutableListOf<GeoPoint>()
                    pastTrips.lastOrNull()?.let { lastVisited ->
                        connectionPoints.add(GeoPoint(lastVisited.latitude, lastVisited.longitude))
                    }
                    upcomingTrips.forEach { upcoming ->
                        connectionPoints.add(GeoPoint(upcoming.latitude, upcoming.longitude))
                    }

                    if (connectionPoints.size >= 2) {
                        val upcomingAnimatedTrail = AnimatedPolylineOverlay(
                            mapView = mapView,
                            glowColorHex = "#4DFFB300", // Translucent Amber Glow
                            coreColorHex = "#FFB300",  // Vibrant Gold Core
                            isDashed = true
                        )
                        upcomingAnimatedTrail.setRoutePoints(connectionPoints)
                        mapView.overlays.add(upcomingAnimatedTrail)
                    }

                    // 3. Add 3D Photo Pin Markers for each Footprint
                    trips.forEachIndexed { index, trip ->
                        val isSelected = selectedTrip?.id == trip.id
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(trip.latitude, trip.longitude)
                            title = trip.title
                            snippet = "${trip.locationName}\n${if (trip.isUpcoming) "Upcoming Destination" else "Visited Location"}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                            // 3D Photo Pin with ambient ground shadow & photo circular window
                            icon = CustomMarkerRenderer.create3DPhotoPin(
                                context = context,
                                trip = trip,
                                index = index + 1,
                                isSelected = isSelected,
                                coroutineScope = coroutineScope
                            ) { updatedIcon ->
                                icon = updatedIcon
                                mapView.invalidate()
                            }

                            // Marker Click Event: Smoothly animate camera center and select trip
                            setOnMarkerClickListener { _, _ ->
                                mapView.controller.animateTo(
                                    GeoPoint(trip.latitude, trip.longitude),
                                    11.5,
                                    650L
                                )
                                onTripSelected(trip)
                                true
                            }
                        }

                        mapView.overlays.add(marker)
                    }
                }

                // If in Map Picker Mode, render a prominent target pin at picked coordinates
                if (isPickerMode && pickedCoordinates != null) {
                    val pickMarker = Marker(mapView).apply {
                        position = GeoPoint(pickedCoordinates.first, pickedCoordinates.second)
                        title = "Selected Location"
                        snippet = "Lat: ${String.format(java.util.Locale.US, "%.4f", pickedCoordinates.first)}, Lng: ${String.format(java.util.Locale.US, "%.4f", pickedCoordinates.second)}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = CustomMarkerRenderer.createStopPin(
                            context = context,
                            stopType = com.lankafootprints.travelapp.data.model.StopType.ATTRACTION,
                            order = 1
                        )
                    }
                    mapView.overlays.add(pickMarker)
                }

                if (homeLocation != null) {
                    val homeMarker = Marker(mapView).apply {
                        position = GeoPoint(homeLocation.first, homeLocation.second)
                        title = "Home"
                        snippet = "Your Home Base"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = CustomMarkerRenderer.createStopPin(
                            context = context,
                            stopType = com.lankafootprints.travelapp.data.model.StopType.START_POINT,
                            order = 0
                        )
                    }
                    mapView.overlays.add(homeMarker)
                }

                mapView.invalidate()
            }
        )

        // Animate Camera to Center Target when changed
        LaunchedEffect(centerTarget, targetZoom) {
            mapViewInstance?.let { map ->
                if (centerTarget != null) {
                    map.controller.animateTo(
                        GeoPoint(centerTarget.first, centerTarget.second),
                        targetZoom,
                        800L
                    )
                }
            }
        }

        // Location Picker Overlay Banner
        if (isPickerMode) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .testTag("picker_mode_banner"),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "Tap anywhere in Sri Lanka to set coordinates",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewInstance?.onDetach()
        }
    }
}

