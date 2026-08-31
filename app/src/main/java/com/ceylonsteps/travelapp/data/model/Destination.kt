package com.ceylonsteps.travelapp.data.model

enum class Province(val displayName: String, val sinhalaName: String) {
    CENTRAL("Central", "මධ්‍යම"),
    SOUTHERN("Southern", "දකුණ"),
    UVA("Uva", "ඌව"),
    EASTERN("Eastern", "නැගෙනහිර"),
    NORTHERN("Northern", "උතුර"),
    NORTH_CENTRAL("North Central", "උතුරු මැද"),
    WESTERN("Western", "බස්නාහිර"),
    NORTH_WESTERN("North Western", "වයඹ"),
    SABARAGAMUWA("Sabaragamuwa", "සබරගමුව")
}

enum class DestinationCategory(val displayName: String) {
    HERITAGE("Heritage"),
    CULTURAL("Cultural"),
    HIKING_NATURE("Hiking & Nature"),
    WATERFALL("Waterfall"),
    BEACH("Beach"),
    WILDLIFE("Wildlife")
}

data class Destination(
    val id: String,
    val name: String,
    val sinhalaName: String,
    val province: Province,
    val district: String,
    val category: DestinationCategory,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val imageUrl: String
)
