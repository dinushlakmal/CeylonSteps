package com.example.util

data class ExplorerRank(
    val level: Int,
    val title: String,
    val starCount: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val progress: Float, // 0.0 to 1.0
    val visitedCount: Int,
    val provincesCount: Int,
    val postsCount: Int,
    val likesCount: Int,
    val description: String
)

object ExplorerRankEngine {

    /**
     * Calculates the user's Explorer Rank Level and XP based on:
     * - Visited destinations (+20 XP each)
     * - Unique provinces covered (+60 XP each)
     * - Shared community stories/posts (+35 XP each)
     * - Likes received on stories (+5 XP each)
     * - Multi-stop journeys completed (+40 XP each)
     */
    fun calculateRank(
        visitedPlacesCount: Int,
        uniqueProvincesCount: Int,
        postsCount: Int,
        totalLikesReceived: Int = 0,
        journeysCount: Int = 0
    ): ExplorerRank {
        val xp = (visitedPlacesCount * 20) +
                (uniqueProvincesCount * 60) +
                (postsCount * 35) +
                (totalLikesReceived * 5) +
                (journeysCount * 40)

        val (level, title, starCount, minXpForLevel, maxXpForLevel, desc) = when {
            xp < 100 -> RankTier(1, "Island Wanderer", 1, 0, 100, "Starting your journey across paradise isle.")
            xp < 250 -> RankTier(2, "Trail Pathfinder", 2, 100, 250, "Actively discovering hidden trails and local gems.")
            xp < 500 -> RankTier(3, "Coastal & Highland Trekker", 3, 250, 500, "Conquering coastal shores and misty tea peaks.")
            xp < 900 -> RankTier(4, "Heritage Voyager", 4, 500, 900, "Immersed in Sri Lanka's ancient kingdoms and wild reserves.")
            xp < 1500 -> RankTier(5, "Ceylon Trailblazer", 5, 900, 1500, "An accomplished voyager charting comprehensive routes.")
            xp < 2500 -> RankTier(6, "Master Ceylon Explorer", 5, 1500, 2500, "Renowned traveler with footprints across the pearl.")
            else -> RankTier(7, "Supreme Ceylon Legend", 5, 2500, 3500, "Ultimate Ceylon travel authority and legendary storyteller.")
        }

        val range = maxXpForLevel - minXpForLevel
        val progressInTier = if (range > 0) {
            ((xp - minXpForLevel).toFloat() / range.toFloat()).coerceIn(0f, 1f)
        } else 1f

        return ExplorerRank(
            level = level,
            title = title,
            starCount = starCount,
            currentXp = xp,
            nextLevelXp = maxXpForLevel,
            progress = progressInTier,
            visitedCount = visitedPlacesCount,
            provincesCount = uniqueProvincesCount,
            postsCount = postsCount,
            likesCount = totalLikesReceived,
            description = desc
        )
    }

    private data class RankTier(
        val level: Int,
        val title: String,
        val stars: Int,
        val minXp: Int,
        val maxXp: Int,
        val description: String
    )
}
