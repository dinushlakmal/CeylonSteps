cat << 'INNER_EOF' > snippet.kt
        combine(
            repository.allTrips,
            timelineRepository.allTripsWithStops,
            userManager.userProfile,
            _filterTab,
            combine(_selectedProvince, recycledTrips) { p, rt -> Pair(p, rt) }
        ) { trips, journeys, profile, filter, provinceAndRecycled ->
            Triple(trips, journeys, Triple(profile, filter, provinceAndRecycled))
        },
INNER_EOF

# Replace lines 122 to 130
sed -i '122,130c\
        combine(\
            repository.allTrips,\
            timelineRepository.allTripsWithStops,\
            userManager.userProfile,\
            _filterTab,\
            combine(_selectedProvince, recycledTrips) { p, rt -> Pair(p, rt) }\
        ) { trips, journeys, profile, filter, provinceAndRecycled ->\
            Triple(trips, journeys, Triple(profile, filter, provinceAndRecycled))\
        },' app/src/main/java/com/example/ui/viewmodel/TripViewModel.kt

