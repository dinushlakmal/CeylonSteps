package com.lankafootprints.travelapp.util

import android.content.Context
import com.example.util.OfflineMapManager as BaseOfflineMapManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import java.io.File

object OfflineMapManager {
    // Sri Lanka Geographic Bounds
    val SRI_LANKA_BOUNDS: BoundingBox = BaseOfflineMapManager.SRI_LANKA_BOUNDS

    val downloadProgress = BaseOfflineMapManager.downloadProgress
    val isOfflineOnlyMode = BaseOfflineMapManager.isOfflineOnlyMode

    fun initializeOfflineCache(context: Context, mapView: MapView) {
        val osmConfig = Configuration.getInstance()
        val basePath = File(context.getExternalFilesDir(null), "osmdroid")
        val tileCache = File(basePath, "tiles")
        if (!tileCache.exists()) tileCache.mkdirs()

        osmConfig.osmdroidBasePath = basePath
        osmConfig.osmdroidTileCache = tileCache
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setUseDataConnection(!BaseOfflineMapManager.isOfflineOnlyMode.value)
    }

    fun downloadRegionTiles(
        context: Context,
        mapView: MapView,
        boundingBox: BoundingBox = SRI_LANKA_BOUNDS,
        minZoom: Int = 7,
        maxZoom: Int = 13,
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

    fun setOfflineOnlyMode(enabled: Boolean, mapView: MapView? = null) {
        BaseOfflineMapManager.setOfflineOnlyMode(enabled, mapView)
    }

    fun downloadSriLankaTiles(
        context: Context,
        mapView: MapView,
        minZoom: Int = 7,
        maxZoom: Int = 13
    ) {
        BaseOfflineMapManager.downloadSriLankaTiles(context, mapView, minZoom, maxZoom)
    }

    fun cancelDownload() {
        BaseOfflineMapManager.cancelDownload()
    }

    suspend fun getCacheSizeInMb(): Double = BaseOfflineMapManager.getCacheSizeInMb()

    suspend fun clearTileCache(): Boolean = BaseOfflineMapManager.clearTileCache()
}
