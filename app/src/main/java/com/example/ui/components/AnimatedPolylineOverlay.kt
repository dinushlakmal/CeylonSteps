package com.example.ui.components

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.view.animation.LinearInterpolator
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

/**
 * Animated Travel Route & Footprint Trail Engine
 *
 * Custom osmdroid Overlay rendering an animated glowing pulse dash trail connecting expedition footprints.
 */
class AnimatedPolylineOverlay(
    private val mapView: MapView,
    glowColorHex: String = "#4D00E5FF", // Translucent Cyan Glow
    coreColorHex: String = "#00E5FF",  // Electric Cyan Core
    val isDashed: Boolean = true
) : Overlay() {

    private val geoPoints = mutableListOf<GeoPoint>()
    private var phase = 0f
    private var animator: ValueAnimator? = null

    // Trail Glow & Dash Paints
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 14f
        color = Color.parseColor(glowColorHex)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor(coreColorHex)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    init {
        startTrailAnimation()
    }

    fun setRoutePoints(points: List<GeoPoint>) {
        geoPoints.clear()
        geoPoints.addAll(points)
        mapView.postInvalidate()
    }

    private fun startTrailAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 60f).apply {
            duration = 1800L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { va ->
                phase = va.animatedValue as Float
                mapView.postInvalidate()
            }
            start()
        }
    }

    override fun draw(canvas: Canvas, projection: org.osmdroid.views.Projection) {
        if (geoPoints.size < 2) return

        val path = Path()
        val screenPoint = Point()

        for (i in geoPoints.indices) {
            projection.toPixels(geoPoints[i], screenPoint)
            if (i == 0) {
                path.moveTo(screenPoint.x.toFloat(), screenPoint.y.toFloat())
            } else {
                path.lineTo(screenPoint.x.toFloat(), screenPoint.y.toFloat())
            }
        }

        // Apply dynamic animated DashPathEffect if dashed mode
        if (isDashed) {
            val dashEffect = DashPathEffect(floatArrayOf(25f, 15f), phase)
            corePaint.pathEffect = dashEffect
        } else {
            corePaint.pathEffect = null
        }

        // Draw Outer Glow and Animated Dashed Core
        canvas.drawPath(path, glowPaint)
        canvas.drawPath(path, corePaint)
    }

    fun onDestroy() {
        animator?.cancel()
        animator = null
    }
}
