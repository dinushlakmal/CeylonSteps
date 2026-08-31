                    // Standard Past/Upcoming Footprints Trail
                    val pastTrips = trips.filter { !it.isUpcoming }.sortedBy { it.dateEpochMillis }
                    val upcomingTrips = trips.filter { it.isUpcoming }.sortedBy { it.dateEpochMillis }

                    val pastPoints = mutableListOf<GeoPoint>()
                    if (homeLocation != null) {
                        pastPoints.add(GeoPoint(homeLocation.first, homeLocation.second))
                    }
                    pastPoints.addAll(pastTrips.map { GeoPoint(it.latitude, it.longitude) })

                    // Past Visited Route (Electric Cyan Glow Animated Trail)
                    if (pastPoints.size >= 2) {
                        val animatedTrail = AnimatedPolylineOverlay(
                            mapView = mapView,
                            glowColorHex = "#4D00E5FF", // Translucent Electric Cyan Glow
                            coreColorHex = "#00E5FF",  // Electric Cyan Core
                            isDashed = true
                        )
                        animatedTrail.setRoutePoints(pastPoints)
                        animatedTrail.drawProgress = scrollProgress
                        activeOverlays.add(animatedTrail)
                        mapView.overlays.add(animatedTrail)
                    }

                    // Upcoming Route Polyline (Gold Trail Connected from last spot or home)
                    val connectionPoints = mutableListOf<GeoPoint>()
                    if (pastTrips.isNotEmpty()) {
                        val lastVisited = pastTrips.last()
                        connectionPoints.add(GeoPoint(lastVisited.latitude, lastVisited.longitude))
                    } else if (homeLocation != null) {
                        connectionPoints.add(GeoPoint(homeLocation.first, homeLocation.second))
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
                        upcomingAnimatedTrail.drawProgress = scrollProgress
                        activeOverlays.add(upcomingAnimatedTrail)
                        mapView.overlays.add(upcomingAnimatedTrail)
                    }
