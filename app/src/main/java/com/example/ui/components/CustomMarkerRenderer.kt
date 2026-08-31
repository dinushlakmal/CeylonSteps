package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.model.TripLocation
import com.example.data.repository.TripRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 3D Photo Pin Marker Generator
 *
 * Renders custom Canvas bitmap markers:
 * - Shape: 3D Teardrop / Map Pin with a tapered bottom tip and ambient elevation shadow.
 * - Inside: A circular window cropped from the user's trip photo thumbnail.
 * - Outer Ring: Vibrant Cyan (Visited), Gold (Upcoming), or Crimson (Selected) border.
 * - Pin Elevation: Soft ground shadow underneath the tip giving a realistic 3D floating look.
 * - Glossy 3D finish: Specular highlight reflection curve and depth bevels.
 */
object CustomMarkerRenderer {

    // Status Colors (Hex)
    val COLOR_CYAN_VISITED = 0xFF00BCD4.toInt()      // Vibrant Cyan (Past / Visited)
    val COLOR_CYAN_DARK = 0xFF00838F.toInt()
    val COLOR_GOLD_UPCOMING = 0xFFFFB300.toInt()     // Vibrant Gold (Upcoming)
    val COLOR_GOLD_DARK = 0xFFE65100.toInt()
    val COLOR_CRIMSON_SELECTED = 0xFFE91E63.toInt()  // Vibrant Crimson (Selected)
    val COLOR_CRIMSON_DARK = 0xFF880E4F.toInt()

    // In-memory LRU cache for cropped thumbnail bitmaps
    private val thumbnailCache = LruCache<String, Bitmap>(50)
    private val markerDrawableCache = LruCache<String, BitmapDrawable>(50)

    /**
     * Synchronously creates a 3D Photo Pin Drawable (using cached photo if available, or fallback gradient),
     * and asynchronously fetches/caches the photo thumbnail using Coil to update the marker once decoded.
     */
    fun create3DPhotoPin(
        context: Context,
        trip: TripLocation,
        index: Int,
        isSelected: Boolean,
        coroutineScope: CoroutineScope? = null,
        onMarkerUpdated: ((BitmapDrawable) -> Unit)? = null
    ): BitmapDrawable {
        val photos = TripRepository.parseJsonArray(trip.imageUrisJson)
        val photoUrl = trip.coverImageUri ?: photos.firstOrNull()

        val cacheKey = "pin_${trip.id}_${isSelected}_${trip.isUpcoming}_${photoUrl.orEmpty()}"
        val cachedDrawable = markerDrawableCache.get(cacheKey)
        if (cachedDrawable != null) {
            return cachedDrawable
        }

        val cachedPhotoBitmap = if (!photoUrl.isNullOrBlank()) thumbnailCache.get(photoUrl) else null

        val drawable = render3DPinBitmap(
            context = context,
            photoBitmap = cachedPhotoBitmap,
            index = index,
            isUpcoming = trip.isUpcoming,
            isSelected = isSelected
        )

        markerDrawableCache.put(cacheKey, drawable)

        // Asynchronously load image if not cached and URL exists
        if (cachedPhotoBitmap == null && !photoUrl.isNullOrBlank() && coroutineScope != null && onMarkerUpdated != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val loadedBitmap = loadBitmapFromUrl(context, photoUrl)
                if (loadedBitmap != null) {
                    thumbnailCache.put(photoUrl, loadedBitmap)
                    val updatedDrawable = render3DPinBitmap(
                        context = context,
                        photoBitmap = loadedBitmap,
                        index = index,
                        isUpcoming = trip.isUpcoming,
                        isSelected = isSelected
                    )
                    markerDrawableCache.put(cacheKey, updatedDrawable)
                    withContext(Dispatchers.Main) {
                        onMarkerUpdated(updatedDrawable)
                    }
                }
            }
        }

        return drawable
    }

    /**
     * Low-level Canvas renderer for the 3D Teardrop Pin Marker
     */
    fun render3DPinBitmap(
        context: Context,
        photoBitmap: Bitmap?,
        index: Int,
        isUpcoming: Boolean,
        isSelected: Boolean
    ): BitmapDrawable {
        val density = context.resources.displayMetrics.density
        val baseWidthDp = if (isSelected) 56f else 48f
        val baseHeightDp = if (isSelected) 74f else 64f

        val width = (baseWidthDp * density).toInt()
        val height = (baseHeightDp * density).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val centerX = width / 2f
        val headRadius = (width / 2f) - (4f * density)
        val headCenterY = headRadius + (3f * density)
        val tipY = height - (9f * density) // tip elevated slightly above ground shadow

        // Primary & Darker status colors
        val (primaryColor, darkColor) = when {
            isSelected -> Pair(COLOR_CRIMSON_SELECTED, COLOR_CRIMSON_DARK)
            isUpcoming -> Pair(COLOR_GOLD_UPCOMING, COLOR_GOLD_DARK)
            else -> Pair(COLOR_CYAN_VISITED, COLOR_CYAN_DARK)
        }

        // ==========================================
        // 1. PIN ELEVATION & SOFT GROUND SHADOW
        // ==========================================
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val shadowRadiusX = headRadius * 0.85f
            val shadowRadiusY = 4f * density
            val shadowCenterY = height - (4.5f * density)
            val shadowRect = RectF(
                centerX - shadowRadiusX,
                shadowCenterY - shadowRadiusY,
                centerX + shadowRadiusX,
                shadowCenterY + shadowRadiusY
            )
            shader = RadialGradient(
                centerX,
                shadowCenterY,
                shadowRadiusX,
                intArrayOf(0x55000000, 0x22000000, 0x00000000),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawOval(
            RectF(
                centerX - (headRadius * 0.85f),
                height - (9f * density),
                centerX + (headRadius * 0.85f),
                height - (0.5f * density)
            ),
            shadowPaint
        )

        // ==========================================
        // 2. 3D TEARDROP / MAP PIN BODY PATH
        // ==========================================
        val pinPath = Path().apply {
            moveTo(centerX, tipY)
            // Left flank curving up into the circle
            quadTo(
                centerX - headRadius - (1.5f * density),
                headCenterY + (headRadius * 0.6f),
                centerX - headRadius,
                headCenterY
            )
            // Top circular arc
            arcTo(
                RectF(
                    centerX - headRadius,
                    headCenterY - headRadius,
                    centerX + headRadius,
                    headCenterY + headRadius
                ),
                180f,
                180f
            )
            // Right flank curving down to bottom tip
            quadTo(
                centerX + headRadius + (1.5f * density),
                headCenterY + (headRadius * 0.6f),
                centerX,
                tipY
            )
            close()
        }

        // Selected Outer Glow Ring
        if (isSelected) {
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x66E91E63.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 6f * density
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            canvas.drawPath(pinPath, glowPaint)
        }

        // 3D Shaded Pin Body (Linear vertical + radial gradient)
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                centerX,
                headCenterY - headRadius,
                centerX,
                tipY,
                intArrayOf(primaryColor, darkColor),
                floatArrayOf(0.1f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawPath(pinPath, bodyPaint)

        // Outer Crisp Bevel Border Ring
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0xFFFFFFFF.toInt()
            strokeWidth = 1.8f * density
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(pinPath, borderPaint)

        // 3D Top-Left Specular Highlight Arc
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0x88FFFFFF.toInt()
            strokeWidth = 2.2f * density
            strokeCap = Paint.Cap.ROUND
        }
        val highlightArcRect = RectF(
            centerX - headRadius + (1.5f * density),
            headCenterY - headRadius + (1.5f * density),
            centerX + headRadius - (1.5f * density),
            headCenterY + headRadius - (1.5f * density)
        )
        canvas.drawArc(highlightArcRect, 200f, 90f, false, highlightPaint)

        // ==========================================
        // 3. CIRCULAR PHOTO WINDOW / THUMBNAIL
        // ==========================================
        val windowRadius = headRadius * 0.72f

        // White Bezel for Photo Window
        val bezelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFFFFFFF.toInt()
        }
        canvas.drawCircle(centerX, headCenterY, windowRadius + (1.5f * density), bezelPaint)

        // Draw Cropped Photo or Fallback
        if (photoBitmap != null) {
            // Draw photo cropped to circle using BitmapShader
            val scaledBitmap = getScaledCroppedSquare(photoBitmap, (windowRadius * 2f).toInt())
            val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            }
            canvas.save()
            canvas.translate(centerX - windowRadius, headCenterY - windowRadius)
            canvas.drawCircle(windowRadius, windowRadius, windowRadius, photoPaint)
            canvas.restore()
        } else {
            // Fallback: Gradient Circle with Number / Star Icon
            val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    centerX - windowRadius,
                    headCenterY - windowRadius,
                    centerX + windowRadius,
                    headCenterY + windowRadius,
                    intArrayOf(0xFFF1F5F9.toInt(), 0xFFCBD5E1.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(centerX, headCenterY, windowRadius, fallbackPaint)

            // Text / Symbol label
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryColor
                textSize = (if (isSelected) 14f else 12f) * density
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val textY = headCenterY - ((textPaint.descent() + textPaint.ascent()) / 2)
            val label = if (isUpcoming) "★" else index.toString()
            canvas.drawText(label, centerX, textY, textPaint)
        }

        // Inner Sub-Bezel Stroke for Photo Window (adds photographic depth)
        val innerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0x33000000.toInt()
            strokeWidth = 1f * density
        }
        canvas.drawCircle(centerX, headCenterY, windowRadius, innerRingPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * Renders a 3D Waypoint Pin for Multi-Stop Timeline Stops
     */
    fun createStopPin(
        context: Context,
        stopType: com.ceylonsteps.travelapp.data.model.StopType,
        order: Int,
        isSelected: Boolean = false
    ): BitmapDrawable {
        val density = context.resources.displayMetrics.density
        val baseWidth = if (isSelected) 44f else 38f
        val baseHeight = if (isSelected) 56f else 48f
        val width = (baseWidth * density).toInt()
        val height = (baseHeight * density).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val primaryColor = when (stopType) {
            com.ceylonsteps.travelapp.data.model.StopType.START_POINT -> 0xFF4CAF50.toInt()
            com.ceylonsteps.travelapp.data.model.StopType.MEAL_BREAK -> 0xFFFF7043.toInt()
            com.ceylonsteps.travelapp.data.model.StopType.FUEL -> 0xFFFFCA28.toInt()
            com.ceylonsteps.travelapp.data.model.StopType.ATTRACTION -> 0xFF00BCD4.toInt()
            com.ceylonsteps.travelapp.data.model.StopType.SCENIC_VIEW -> 0xFF8BC34A.toInt()
            com.ceylonsteps.travelapp.data.model.StopType.HOTEL -> 0xFF9C27B0.toInt()
            com.ceylonsteps.travelapp.data.model.StopType.END_POINT -> 0xFFE91E63.toInt()
        }

        val centerX = width / 2f
        val headRadius = (baseWidth / 2f - 3f) * density
        val headCenterY = headRadius + 3f * density
        val tipY = height - 5f * density

        // 1. Soft Shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x55000000.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawOval(
            RectF(centerX - 10f * density, height - 6f * density, centerX + 10f * density, height - 1f * density),
            shadowPaint
        )

        // 2. Pin Body Path
        val pinPath = Path().apply {
            moveTo(centerX, tipY)
            val angle = Math.toRadians(35.0)
            val dx = (Math.cos(angle) * headRadius).toFloat()
            val dy = (Math.sin(angle) * headRadius).toFloat()
            quadTo(centerX - headRadius * 0.9f, headCenterY + headRadius * 0.5f, centerX - dx, headCenterY + dy)
            arcTo(
                RectF(centerX - headRadius, headCenterY - headRadius, centerX + headRadius, headCenterY + headRadius),
                180f - 35f,
                250f,
                false
            )
            quadTo(centerX + headRadius * 0.9f, headCenterY + headRadius * 0.5f, centerX, tipY)
            close()
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = primaryColor
        }
        canvas.drawPath(pinPath, bodyPaint)

        // 3. Inner White Circle
        val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = AndroidColor.WHITE
        }
        canvas.drawCircle(centerX, headCenterY, headRadius * 0.72f, innerCirclePaint)

        // 4. Emoji / Text
        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (if (isSelected) 14f else 12f) * density
            textAlign = Paint.Align.CENTER
        }
        val textY = headCenterY - ((emojiPaint.descent() + emojiPaint.ascent()) / 2)
        canvas.drawText(stopType.emoji, centerX, textY, emojiPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * Scales and center-crops a Bitmap to fit a target square size
     */
    private fun getScaledCroppedSquare(source: Bitmap, targetSize: Int): Bitmap {
        if (targetSize <= 0) return source
        val minDim = Math.min(source.width, source.height)
        val xOffset = (source.width - minDim) / 2
        val yOffset = (source.height - minDim) / 2

        val cropped = Bitmap.createBitmap(source, xOffset, yOffset, minDim, minDim)
        return Bitmap.createScaledBitmap(cropped, targetSize, targetSize, true)
    }

    /**
     * Loads a Bitmap asynchronously from a URL using Coil
     */
    private suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .size(160, 160)
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                result.drawable.toBitmap()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
