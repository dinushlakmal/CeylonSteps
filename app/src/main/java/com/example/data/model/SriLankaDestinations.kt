package com.example.data.model

import com.lankafootprints.travelapp.data.model.Destination
import com.lankafootprints.travelapp.data.model.DestinationCategory
import com.lankafootprints.travelapp.data.seed.DestinationDataSeeder

data class SriLankaLandmark(
    val id: String = "",
    val title: String,
    val sinhalaTitle: String = "",
    val locationName: String,
    val district: String = "",
    val province: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val category: String, // "Heritage", "Cultural", "Hiking & Nature", "Waterfall", "Beach", "Wildlife", "Adventure", "City"
    val elevationMeters: Int = 100,
    val bestTimeToVisit: String = "Year-round",
    val defaultCoverUri: String
)

object SriLankaDestinations {

    val PROVINCES = listOf(
        "Central",
        "Southern",
        "Uva",
        "Eastern",
        "Northern",
        "North Central",
        "Western",
        "North Western",
        "Sabaragamuwa"
    )

    val PROVINCE_SINHALA = mapOf(
        "Central" to "මධ්‍යම පළාත",
        "Southern" to "දකුණු පළාත",
        "Uva" to "ඌව පළාත",
        "Eastern" to "නැගෙනහිර පළාත",
        "Northern" to "උතුරු පළාත",
        "North Central" to "උතුරු මැද පළාත",
        "Western" to "බස්නාහිර පළාත",
        "North Western" to "වයඹ පළාත",
        "Sabaragamuwa" to "සබරගමු පළාත"
    )

    val CATEGORIES = listOf(
        "All",
        "Heritage",
        "Cultural",
        "Hiking & Nature",
        "Waterfall",
        "Beach",
        "Wildlife"
    )

    // Complete 100+ Sri Lankan destinations sourced directly from DestinationDataSeeder
    val PRESET_LANDMARKS: List<SriLankaLandmark> by lazy {
        try {
            DestinationDataSeeder.get100PlusDestinations().map { dest ->
                dest.toLandmark()
            }
        } catch (e: Exception) {
            FALLBACK_LANDMARKS
        }
    }

    val ALL_DESTINATIONS: List<SriLankaLandmark> get() = PRESET_LANDMARKS

    fun Destination.toLandmark(): SriLankaLandmark {
        val catDisplay = when (category) {
            DestinationCategory.HERITAGE -> "Heritage"
            DestinationCategory.CULTURAL -> "Cultural"
            DestinationCategory.HIKING_NATURE -> "Hiking & Nature"
            DestinationCategory.WATERFALL -> "Waterfall"
            DestinationCategory.BEACH -> "Beach"
            DestinationCategory.WILDLIFE -> "Wildlife"
        }

        val bestTime = when (category) {
            DestinationCategory.BEACH -> "Morning (6:30 AM) or Sunset (5:45 PM)"
            DestinationCategory.WATERFALL -> "Morning (8:00 AM - 11:00 AM)"
            DestinationCategory.HIKING_NATURE -> "Early Morning (6:00 AM) for sunrise"
            DestinationCategory.WILDLIFE -> "Early Morning 6:00 AM or 3:00 PM Safari"
            DestinationCategory.HERITAGE, DestinationCategory.CULTURAL -> "Morning Pooja or 4:00 PM"
        }

        val approxElevation = when (id) {
            "cp_01" -> 349 // Sigiriya
            "cp_02" -> 500 // Kandy Tooth Temple
            "cp_03" -> 200 // Pidurangala
            "cp_04" -> 1863 // Knuckles
            "cp_05" -> 2100 // Horton Plains
            "cp_09" -> 1065 // Ambuluwawa
            "up_01" -> 1041 // Nine Arch
            "up_02" -> 1141 // Little Adam's Peak
            "up_03" -> 220  // Diyaluma
            "up_06" -> 263  // Bambarakanda
            "up_08" -> 1970 // Lipton's Seat
            "sab_01" -> 2243 // Sri Pada / Adam's peak
            else -> when (province.displayName) {
                "Central" -> 750
                "Uva" -> 850
                "Sabaragamuwa" -> 450
                "Western" -> 15
                "Southern" -> 20
                "Eastern" -> 12
                "Northern" -> 8
                "North Central" -> 90
                "North Western" -> 35
                else -> 50
            }
        }

        return SriLankaLandmark(
            id = id,
            title = name,
            sinhalaTitle = sinhalaName,
            locationName = "$name, $district",
            district = district,
            province = province.displayName,
            latitude = latitude,
            longitude = longitude,
            description = description,
            category = catDisplay,
            elevationMeters = approxElevation,
            bestTimeToVisit = bestTime,
            defaultCoverUri = imageUrl
        )
    }

    private val FALLBACK_LANDMARKS = listOf(
        SriLankaLandmark(
            id = "cp_01",
            title = "Sigiriya Rock Fortress",
            sinhalaTitle = "සීගිරිය",
            locationName = "Sigiriya, Matale",
            district = "Matale",
            province = "Central",
            latitude = 7.9570,
            longitude = 80.7603,
            description = "The ancient 5th-century palace citadel atop a dramatic 200m monolithic column of granite rock. Renowned for vibrant frescoes and lion paw entryway.",
            category = "Heritage",
            elevationMeters = 349,
            bestTimeToVisit = "Early morning (6:30 AM) for sunrise and cooler climb",
            defaultCoverUri = "https://images.unsplash.com/photo-1588598198321-9735fd52455b?w=1000&auto=format&fit=crop&q=80"
        ),
        SriLankaLandmark(
            id = "up_01",
            title = "Nine Arch Bridge & Tea Hills",
            sinhalaTitle = "ආරුක්කු නමයේ පාලම",
            locationName = "Ella, Badulla",
            district = "Badulla",
            province = "Uva",
            latitude = 6.8768,
            longitude = 81.0608,
            description = "Iconic colonial-era stone train viaduct curved gracefully through lush emerald tea estates and misty mountain valleys in the Ceylon highlands.",
            category = "Hiking & Nature",
            elevationMeters = 1041,
            bestTimeToVisit = "9:15 AM or 11:30 AM to catch the blue passenger train crossing",
            defaultCoverUri = "https://images.unsplash.com/photo-1546708973-b339540b5162?w=1000&auto=format&fit=crop&q=80"
        )
    )

    fun findMatchingProvince(lat: Double, lng: Double): String {
        return when {
            lat >= 9.0 -> "Northern"
            lat >= 8.2 && lng <= 80.8 -> "North Central"
            lat >= 8.0 && lng > 80.8 -> "Eastern"
            lat in 7.0..8.2 && lng in 79.7..80.4 -> "North Western"
            lat in 7.0..7.6 && lng in 80.4..81.0 -> "Central"
            lat in 6.7..7.4 && lng < 80.2 -> "Western"
            lat in 6.4..7.2 && lng in 80.8..81.8 -> "Uva"
            lat in 6.5..7.1 && lng in 80.2..80.7 -> "Sabaragamuwa"
            lat < 6.4 -> "Southern"
            else -> "Central"
        }
    }

    fun searchDestinations(query: String): List<SriLankaLandmark> {
        if (query.isBlank()) return PRESET_LANDMARKS
        val q = query.trim().lowercase()
        return PRESET_LANDMARKS.filter {
            it.title.lowercase().contains(q) ||
                    it.sinhalaTitle.lowercase().contains(q) ||
                    it.locationName.lowercase().contains(q) ||
                    it.district.lowercase().contains(q) ||
                    it.province.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
        }
    }
}
