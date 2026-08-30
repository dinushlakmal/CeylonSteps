package com.example.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import java.io.File

object OfflineMapManager {

    // Bounding Box strictly encompassing Sri Lanka landmass & territorial coastal waters
    val SRI_LANKA_BOUNDS = BoundingBox(9.90, 81.90, 5.85, 79.60)

    // Recommended zoom range for high detail offline caching without excessive storage
    const val MIN_OFFLINE_ZOOM = 7
    const val MAX_OFFLINE_ZOOM = 13

    data class DownloadProgress(
        val isDownloading: Boolean = false,
        val currentProgress: Int = 0, // 0 - 100%
        val tilesDownloaded: Int = 0,
        val totalTiles: Int = 0,
        val statusMessage: String = "",
        val isCompleted: Boolean = false,
        val error: String? = null
    )

    private val _downloadProgress = MutableStateFlow(DownloadProgress())
    val downloadProgress: StateFlow<DownloadProgress> = _downloadProgress.asStateFlow()

    private val _isOfflineOnlyMode = MutableStateFlow(false)
    val isOfflineOnlyMode: StateFlow<Boolean> = _isOfflineOnlyMode.asStateFlow()

    private var activeCacheTask: CacheManager.CacheManagerTask? = null

    fun setOfflineOnlyMode(enabled: Boolean, mapView: MapView? = null) {
        _isOfflineOnlyMode.value = enabled
        mapView?.setUseDataConnection(!enabled)
    }

    /**
     * Initializes offline tile directories and configuration for MapView
     */
    fun initializeOfflineCache(context: Context, mapView: MapView) {
        val osmConfig = Configuration.getInstance()
        val basePath = File(context.getExternalFilesDir(null), "osmdroid")
        val tileCache = File(basePath, "tiles")
        if (!tileCache.exists()) tileCache.mkdirs()

        osmConfig.osmdroidBasePath = basePath
        osmConfig.osmdroidTileCache = tileCache
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setUseDataConnection(!_isOfflineOnlyMode.value)
    }

    /**
     * Downloads custom bounding box region tiles
     */
    fun downloadRegionTiles(
        context: Context,
        mapView: MapView,
        boundingBox: BoundingBox = SRI_LANKA_BOUNDS,
        minZoom: Int = MIN_OFFLINE_ZOOM,
        maxZoom: Int = MAX_OFFLINE_ZOOM,
        onProgress: (Int) -> Unit = {}
    ) {
        val cacheManager = CacheManager(mapView)
        cacheManager.downloadAreaAsync(
            context,
            boundingBox,
            minZoom,
            maxZoom,
            object : CacheManager.CacheManagerCallback {
                override fun onTaskComplete() {
                    onProgress(100)
                }

                override fun updateProgress(progress: Int, current: Int, total: Int, zoom: Int) {
                    val percent = if (total > 0) ((current.toFloat() / total.toFloat()) * 100).toInt() else progress
                    onProgress(percent.coerceIn(0, 100))
                }

                override fun downloadStarted() {}

                override fun setPossibleTilesInArea(total: Int) {}

                override fun onTaskFailed(errors: Int) {}
            }
        )
    }

    /**
     * Estimates the total tile count for Sri Lanka bounding box across zoom range
     */
    fun estimateSriLankaTileCount(
        context: Context,
        minZoom: Int = MIN_OFFLINE_ZOOM,
        maxZoom: Int = MAX_OFFLINE_ZOOM
    ): Int {
        return try {
            val dummyMapView = MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
            }
            val cacheManager = CacheManager(dummyMapView)
            cacheManager.possibleTilesInArea(SRI_LANKA_BOUNDS, minZoom, maxZoom)
        } catch (e: Exception) {
            1850 // Typical default estimate for z7..z13
        }
    }

    /**
     * Downloads and caches all OSM tiles for Sri Lanka in the background
     */
    fun downloadSriLankaTiles(
        context: Context,
        mapView: MapView,
        minZoom: Int = MIN_OFFLINE_ZOOM,
        maxZoom: Int = MAX_OFFLINE_ZOOM
    ) {
        if (_downloadProgress.value.isDownloading) return

        try {
            val cacheManager = CacheManager(mapView)
            val totalTiles = cacheManager.possibleTilesInArea(SRI_LANKA_BOUNDS, minZoom, maxZoom)

            _downloadProgress.value = DownloadProgress(
                isDownloading = true,
                currentProgress = 0,
                tilesDownloaded = 0,
                totalTiles = totalTiles,
                statusMessage = "Starting Sri Lanka offline map download ($totalTiles tiles)..."
            )

            activeCacheTask = cacheManager.downloadAreaAsync(
                context,
                SRI_LANKA_BOUNDS,
                minZoom,
                maxZoom,
                object : CacheManager.CacheManagerCallback {
                    override fun onTaskComplete() {
                        _downloadProgress.value = DownloadProgress(
                            isDownloading = false,
                            currentProgress = 100,
                            tilesDownloaded = totalTiles,
                            totalTiles = totalTiles,
                            statusMessage = "100% Sri Lanka Offline Map Cached Successfully!",
                            isCompleted = true
                        )
                        activeCacheTask = null
                    }

                    override fun onTaskFailed(errors: Int) {
                        _downloadProgress.value = DownloadProgress(
                            isDownloading = false,
                            currentProgress = _downloadProgress.value.currentProgress,
                            tilesDownloaded = _downloadProgress.value.tilesDownloaded,
                            totalTiles = totalTiles,
                            statusMessage = "Completed with $errors network timeouts. Cached tiles are ready for offline use.",
                            isCompleted = true,
                            error = if (errors > 0) "Some tiles could not be fetched, retry if needed" else null
                        )
                        activeCacheTask = null
                    }

                    override fun updateProgress(progress: Int, current: Int, total: Int, zoom: Int) {
                        val pct = if (total > 0) ((current.toFloat() / total.toFloat()) * 100).toInt() else progress
                        _downloadProgress.value = DownloadProgress(
                            isDownloading = true,
                            currentProgress = pct.coerceIn(0, 100),
                            tilesDownloaded = current,
                            totalTiles = total,
                            statusMessage = "Caching Zoom Level $zoom: $current / $total tiles ($pct%)"
                        )
                    }

                    override fun downloadStarted() {
                        _downloadProgress.value = _downloadProgress.value.copy(
                            statusMessage = "Downloading Sri Lanka Map tiles..."
                        )
                    }

                    override fun setPossibleTilesInArea(total: Int) {
                        // update total
                    }
                }
            )
        } catch (e: Exception) {
            _downloadProgress.value = DownloadProgress(
                isDownloading = false,
                error = e.localizedMessage ?: "Failed to initiate tile caching"
            )
        }
    }

    /**
     * Cancels active download task if running
     */
    fun cancelDownload() {
        activeCacheTask?.let {
            if (!it.isCancelled) {
                it.cancel(true)
            }
        }
        _downloadProgress.value = DownloadProgress(
            isDownloading = false,
            statusMessage = "Download cancelled by explorer."
        )
    }

    /**
     * Calculates the cache directory size in MB
     */
    suspend fun getCacheSizeInMb(): Double = withContext(Dispatchers.IO) {
        try {
            val tileCacheDir = Configuration.getInstance().osmdroidTileCache
            if (tileCacheDir != null && tileCacheDir.exists()) {
                val bytes = getFolderSize(tileCacheDir)
                return@withContext bytes / (1024.0 * 1024.0)
            }
            0.0
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Clears all cached osmdroid tiles
     */
    suspend fun clearTileCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            val tileCacheDir = Configuration.getInstance().osmdroidTileCache
            if (tileCacheDir != null && tileCacheDir.exists()) {
                deleteRecursive(tileCacheDir)
                _downloadProgress.value = DownloadProgress(
                    isDownloading = false,
                    currentProgress = 0,
                    statusMessage = "Offline map cache purged."
                )
                return@withContext true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun getFolderSize(file: File): Long {
        var size: Long = 0
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                size += getFolderSize(child)
            }
        } else {
            size = file.length()
        }
        return size
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach { child ->
                deleteRecursive(child)
            }
        }
        fileOrDirectory.delete()
    }
}
