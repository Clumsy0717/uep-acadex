package com.example.acadex

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Zoomable campus map. Draws either a clean 2D redraw of the campus or the satellite photo,
 * then draws crisp pins on top (pins keep a constant on-screen size at every zoom level).
 */
class CampusMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class MapStyle { FLAT_2D, SATELLITE }

    var onPlaceSelected: ((CampusPlace?) -> Unit)? = null

    var satelliteBitmap: Bitmap? = null
        set(value) { field = value; invalidate() }

    var mapStyle: MapStyle = MapStyle.FLAT_2D
        set(value) { field = value; invalidate() }

    var selectedPlace: CampusPlace? = null
        private set

    private var visiblePlaces: List<CampusPlace> = CampusData.places

    private val dp = resources.displayMetrics.density
    private val mapMatrix = Matrix()
    private val inverse = Matrix()
    private val values = FloatArray(9)
    private val tmp = FloatArray(2)

    private var fitScale = 1f
    private var maxScale = 6f
    private var cameraAnimator: ValueAnimator? = null

    // ---- selection animation state
    private var popProgress = 1f
    private var pulse = 0f
    private val popAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 380
        interpolator = OvershootInterpolator(2.2f)
        addUpdateListener { popProgress = it.animatedValue as Float; invalidate() }
    }
    private val pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1500
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { pulse = it.animatedValue as Float; invalidate() }
    }

    // ---- prebuilt paths (map space)
    private val roadPath = polygon(CampusGeometry.road)
    private val vegetationPath = polygon(CampusGeometry.vegetation)
    private val walkwayPaths = CampusGeometry.walkways.map { polygon(it) }
    private val parkingPaths = CampusGeometry.parking.map { polygon(it) }
    private val buildingPaths = CampusGeometry.buildings.map { it to polygon(it.pts) }
    private val areaPaths = CampusGeometry.areas.map { it to polygon(it.pts) }

    // ---- preallocated drawing objects to avoid allocation in onDraw
    private val reusablePinPath = Path()
    private val reusableRect = RectF()
    private val mapBoundsRect = RectF(0f, 0f, CampusData.MAP_W, CampusData.MAP_H)
    private val takenLabels = ArrayList<RectF>()
    private val dashEffect = DashPathEffect(floatArrayOf(14f, 12f), 0f)
    private val cameraStartValues = FloatArray(9)
    private val cameraEndValues = FloatArray(9)
    private val cameraNowValues = FloatArray(9)

    // ---- paints
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        textSize = 12f * dp
        textAlign = Paint.Align.CENTER
    }
    private val iconCache = HashMap<PlaceCategory, Drawable?>()

    // ---- gestures
    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                cameraAnimator?.cancel()
                val current = currentScale()
                val target = (current * detector.scaleFactor).coerceIn(fitScale, maxScale)
                val factor = target / current
                mapMatrix.postScale(factor, factor, detector.focusX, detector.focusY)
                clampMatrix()
                invalidate()
                return true
            }
        })

    private val gestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
                if (scaleDetector.isInProgress) return false
                cameraAnimator?.cancel()
                mapMatrix.postTranslate(-dx, -dy)
                clampMatrix()
                invalidate()
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                handleTap(e.x, e.y)
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                val zoomedIn = currentScale() > fitScale * 1.8f
                val target = if (zoomedIn) fitScale else min(maxScale, fitScale * 2.8f)
                tmp[0] = e.x; tmp[1] = e.y
                mapMatrix.invert(inverse)
                inverse.mapPoints(tmp)
                animateCamera(target, tmp[0], tmp[1], e.x, e.y)
                return true
            }
        })

    // =====================================================================================
    // Public API
    // =====================================================================================

    @Suppress("unused")
    fun setVisiblePlaces(places: List<CampusPlace>) {
        visiblePlaces = places
        if (selectedPlace != null && selectedPlace !in places) selectPlace(null, notify = true)
        invalidate()
    }

    fun selectPlace(place: CampusPlace?, animate: Boolean = true, notify: Boolean = true) {
        selectedPlace = place
        if (place != null) {
            popAnimator.cancel(); popAnimator.start()
            if (!pulseAnimator.isRunning) pulseAnimator.start()
            if (animate) {
                val targetScale = max(currentScale(), min(maxScale, fitScale * 2.4f))
                animateCamera(targetScale, place.x, place.y, width / 2f, height * 0.36f)
            }
        } else {
            pulseAnimator.cancel()
        }
        if (notify) onPlaceSelected?.invoke(place)
        invalidate()
    }

    @Suppress("unused")
    fun resetView() {
        selectPlace(null)
        animateCamera(fitScale, CampusData.MAP_W / 2f, CampusData.MAP_H / 2f, width / 2f, height / 2f)
    }

    @Suppress("unused")
    fun zoomBy(factor: Float) {
        val cx = width / 2f
        val cy = height / 2f
        tmp[0] = cx; tmp[1] = cy
        mapMatrix.invert(inverse)
        inverse.mapPoints(tmp)
        animateCamera((currentScale() * factor).coerceIn(fitScale, maxScale), tmp[0], tmp[1], cx, cy)
    }

    // =====================================================================================
    // Layout / matrix helpers
    // =====================================================================================

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return
        fitScale = min(w / CampusData.MAP_W, h / CampusData.MAP_H)
        maxScale = fitScale * 7f
        mapMatrix.reset()
        mapMatrix.postScale(fitScale, fitScale)
        clampMatrix()
    }

    private fun currentScale(): Float {
        mapMatrix.getValues(values)
        return values[Matrix.MSCALE_X]
    }

    private fun clampMatrix() {
        val r = RectF(0f, 0f, CampusData.MAP_W, CampusData.MAP_H)
        mapMatrix.mapRect(r)
        var dx = 0f
        var dy = 0f
        if (r.width() <= width) dx = width / 2f - r.centerX()
        else if (r.left > 0) dx = -r.left
        else if (r.right < width) dx = width - r.right
        if (r.height() <= height) dy = height / 2f - r.centerY()
        else if (r.top > 0) dy = -r.top
        else if (r.bottom < height) dy = height - r.bottom
        mapMatrix.postTranslate(dx, dy)
    }

    private fun animateCamera(scale: Float, mx: Float, my: Float, sx: Float, sy: Float) {
        cameraAnimator?.cancel()
        mapMatrix.getValues(cameraStartValues)
        val end = Matrix().apply {
            postScale(scale, scale)
            postTranslate(sx - mx * scale, sy - my * scale)
        }
        end.getValues(cameraEndValues)
        cameraAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 420
            interpolator = DecelerateInterpolator(1.6f)
            addUpdateListener {
                val t = it.animatedValue as Float
                for (i in 0 until 9) cameraNowValues[i] = cameraStartValues[i] + (cameraEndValues[i] - cameraStartValues[i]) * t
                mapMatrix.setValues(cameraNowValues)
                clampMatrix()
                invalidate()
            }
            start()
        }
    }

    // =====================================================================================
    // Touch
    // =====================================================================================

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        val handled = scaleDetector.onTouchEvent(event) or gestureDetector.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            performClick()
        }
        return handled || super.onTouchEvent(event)
    }

    private fun handleTap(x: Float, y: Float) {
        performClick()
        // 1) pins (screen space), top-most first
        val pinHit = visiblePlaces
            .sortedByDescending { screenY(it) }
            .firstOrNull { place ->
                val ax = screenX(place)
                val ay = screenY(place)
                val r = pinRadius(place)
                val cy = ay - r * 1.9f
                hypot(x - ax, y - cy) <= r + 8 * dp || hypot(x - ax, y - ay) <= 16 * dp
            }
        if (pinHit != null) {
            selectPlace(if (pinHit == selectedPlace) null else pinHit)
            return
        }
        // 2) building / area shapes (map space)
        tmp[0] = x; tmp[1] = y
        mapMatrix.invert(inverse)
        inverse.mapPoints(tmp)
        val mx = tmp[0]
        val my = tmp[1]
        val shapeHit = (CampusGeometry.buildings + CampusGeometry.areas)
            .firstOrNull { it.placeId != null && pointInPolygon(mx, my, it.pts) }
        val place = shapeHit?.let { s -> visiblePlaces.firstOrNull { it.id == s.placeId } }
        selectPlace(place)
    }

    // =====================================================================================
    // Drawing
    // =====================================================================================

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(if (mapStyle == MapStyle.SATELLITE) 0xFF1B2A1F.toInt() else 0xFFEAF0E4.toInt())

        canvas.withMatrix(mapMatrix) {
            if (mapStyle == MapStyle.SATELLITE && satelliteBitmap != null) {
                drawBitmap(satelliteBitmap!!, null, mapBoundsRect, bitmapPaint)
            } else {
                draw2D(this)
            }
            drawSelectionOutline(this)
        }

        drawMarkers(canvas)
    }

    private fun draw2D(c: Canvas) {
        fill.color = 0xFFD3E6C7.toInt(); c.drawPath(vegetationPath, fill)
        fill.color = 0xFFE1DED7.toInt(); c.drawPath(roadPath, fill)
        stroke.color = Color.WHITE
        stroke.strokeWidth = 2.5f
        stroke.pathEffect = dashEffect
        c.drawLine(187f, 0f, 15f, 430f, stroke)
        stroke.pathEffect = null
        walkwayPaths.forEach {
            fill.color = 0xFFF7F4EC.toInt(); c.drawPath(it, fill)
        }

        // soccer field with mowing stripes and markings
        val f = CampusGeometry.field
        val left = f[0]; val top = f[1]; val right = f[4]; val bottom = f[5]
        fill.color = 0xFF93C984.toInt()
        c.drawRect(left, top, right, bottom, fill)
        val stripeW = (right - left) / 10f
        fill.color = 0x1A2E7D32
        for (i in 0 until 10 step 2) c.drawRect(left + i * stripeW, top, left + (i + 1) * stripeW, bottom, fill)
        stroke.color = 0xCCFFFFFF.toInt(); stroke.strokeWidth = 2f
        c.drawRect(left + 12, top + 12, right - 12, bottom - 12, stroke)
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        c.drawLine(cx, top + 12, cx, bottom - 12, stroke)
        c.drawCircle(cx, cy, 44f, stroke)
        c.drawRect(left + 12, cy - 75f, left + 70f, cy + 75f, stroke)
        c.drawRect(right - 70f, cy - 75f, right - 12, cy + 75f, stroke)

        // parking with stall lines
        parkingPaths.forEach { fill.color = 0xFFD9DEE7.toInt(); c.drawPath(it, fill) }
        stroke.color = 0xFFFFFFFF.toInt(); stroke.strokeWidth = 1.5f
        var x = 262f
        while (x < 1075f) { c.drawLine(x, 606f, x, 633f, stroke); x += 15f }

        // building shadows, then roofs
        fill.color = 0x3C1E281E
        buildingPaths.forEach { (_, p) ->
            c.withTranslation(5f, 6f) {
                drawPath(p, fill)
            }
        }
        buildingPaths.forEach { (shape, p) ->
            val (roof, edge) = roofColors(shape.category)
            fill.color = roof; c.drawPath(p, fill)
            stroke.color = edge; stroke.strokeWidth = 2f; c.drawPath(p, stroke)
        }

        // trees
        val t = CampusGeometry.trees
        var i = 0
        while (i < t.size) {
            fill.color = 0x30124012; c.drawCircle(t[i] + 3f, t[i + 1] + 4f, t[i + 2], fill)
            fill.color = 0xFF74AB62.toInt(); c.drawCircle(t[i], t[i + 1], t[i + 2], fill)
            fill.color = 0x40FFFFFF; c.drawCircle(t[i] - t[i + 2] * 0.25f, t[i + 1] - t[i + 2] * 0.25f, t[i + 2] * 0.55f, fill)
            i += 3
        }
    }

    private fun roofColors(cat: PlaceCategory?): Pair<Int, Int> = when (cat) {
        PlaceCategory.ACADEMIC -> 0xFFA9C4F5.toInt() to 0xFF5E86D8.toInt()
        PlaceCategory.ADMIN -> 0xFFCDB5EE.toInt() to 0xFF8E5BD9.toInt()
        PlaceCategory.TECH -> 0xFF8ED8D0.toInt() to 0xFF14A89A.toInt()
        PlaceCategory.HEALTH -> 0xFFF4A9A9.toInt() to 0xFFD94A4F.toInt()
        PlaceCategory.FOOD -> 0xFFF8CB8E.toInt() to 0xFFE08420.toInt()
        PlaceCategory.RECREATION -> 0xFF8FC77F.toInt() to 0xFF3E9B4F.toInt()
        PlaceCategory.PARKING -> 0xFFD3D9E3.toInt() to 0xFF5C6B80.toInt()
        null -> 0xFFE3DED6.toInt() to 0xFFB9B2A6.toInt()
    }

    private fun drawSelectionOutline(c: Canvas) {
        val sel = selectedPlace ?: return
        val s = currentScale()
        val shapes = (buildingPaths + areaPaths).filter { it.first.placeId == sel.id }
        shapes.forEach { (_, p) ->
            fill.color = (sel.category.color and 0x00FFFFFF) or 0x33000000
            c.drawPath(p, fill)
            stroke.color = sel.category.color
            stroke.strokeWidth = 3f * dp / s
            c.drawPath(p, stroke)
        }
    }

    // ---- markers ------------------------------------------------------------------------

    private fun screenX(p: CampusPlace): Float {
        tmp[0] = p.x; tmp[1] = p.y; mapMatrix.mapPoints(tmp); return tmp[0]
    }

    private fun screenY(p: CampusPlace): Float {
        tmp[0] = p.x; tmp[1] = p.y; mapMatrix.mapPoints(tmp); return tmp[1]
    }

    private fun pinRadius(p: CampusPlace): Float {
        val base = 15f * dp
        return if (p == selectedPlace) base * (1f + 0.35f * popProgress) else base
    }

    private fun drawMarkers(c: Canvas) {
        val zoomedEnough = currentScale() >= fitScale * 1.7f
        val ordered = visiblePlaces.sortedBy { screenY(it) }
        val sel = selectedPlace
        val drawOrder = if (sel != null && sel in ordered) ordered.filter { it != sel } + sel else ordered

        // pass 1: pins
        drawOrder.forEach { drawPin(c, it) }

        // pass 2: labels (selected first so it always wins collisions)
        takenLabels.clear()
        val labelOrder = if (sel != null && sel in ordered) listOf(sel) + ordered.filter { it != sel } else ordered
        labelOrder.forEach { place ->
            if (place == sel || zoomedEnough) drawLabel(c, place, takenLabels)
        }
    }

    private fun drawPin(c: Canvas, place: CampusPlace) {
        val ax = screenX(place)
        val ay = screenY(place)
        if (ax < -60 * dp || ay < -80 * dp || ax > width + 60 * dp || ay > height + 60 * dp) return
        val r = pinRadius(place)
        val cy = ay - r * 1.9f
        val color = place.category.color
        val selected = place == selectedPlace

        if (selected) {
            fill.color = (color and 0x00FFFFFF) or (((1f - pulse) * 90).toInt() shl 24)
            c.drawCircle(ax, ay, (8f + 34f * pulse) * dp, fill)
        }
        fill.color = 0x40000000
        c.drawOval(ax - r * 0.55f, ay - r * 0.16f, ax + r * 0.55f, ay + r * 0.16f, fill)

        reusablePinPath.reset()
        reusablePinPath.moveTo(ax, ay)
        reusablePinPath.lineTo(ax - r * 0.78f, cy + r * 0.62f)
        reusablePinPath.lineTo(ax + r * 0.78f, cy + r * 0.62f)
        reusablePinPath.close()

        fill.color = Color.WHITE
        c.drawCircle(ax, cy, r + 2.5f * dp, fill)
        stroke.color = Color.WHITE; stroke.strokeWidth = 5f * dp
        c.drawPath(reusablePinPath, stroke)
        c.drawPath(reusablePinPath, fill)
        fill.color = color
        c.drawPath(reusablePinPath, fill)
        c.drawCircle(ax, cy, r, fill)

        val icon = iconCache.getOrPut(place.category) {
            ContextCompat.getDrawable(context, place.category.icon)?.mutate()
        }
        icon?.let {
            val half = r * 0.58f
            it.setBounds((ax - half).toInt(), (cy - half).toInt(), (ax + half).toInt(), (cy + half).toInt())
            it.setTint(Color.WHITE)
            it.draw(c)
        }
    }

    private fun drawLabel(c: Canvas, place: CampusPlace, taken: MutableList<RectF>) {
        val ax = screenX(place)
        val ay = screenY(place)
        val padH = 8f * dp
        val textW = labelPaint.measureText(place.name)
        reusableRect.set(
            ax - textW / 2 - padH, ay + 7f * dp,
            ax + textW / 2 + padH, ay + 7f * dp + 24f * dp
        )
        if (reusableRect.right < 0 || reusableRect.left > width || reusableRect.top > height) return
        val isSel = place == selectedPlace
        if (!isSel && taken.any { RectF.intersects(it, reusableRect) }) return
        val savedRect = RectF(reusableRect)
        taken.add(savedRect)

        fill.color = 0x26000000
        reusableRect.offset(0f, 1.5f * dp)
        c.drawRoundRect(reusableRect, 12f * dp, 12f * dp, fill)
        reusableRect.offset(0f, -1.5f * dp)
        fill.color = if (isSel) place.category.color else 0xF2FFFFFF.toInt()
        c.drawRoundRect(reusableRect, 12f * dp, 12f * dp, fill)
        labelPaint.color = if (isSel) Color.WHITE else 0xFF1F2933.toInt()
        val baseline = reusableRect.centerY() - (labelPaint.descent() + labelPaint.ascent()) / 2
        c.drawText(place.name, reusableRect.centerX(), baseline, labelPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cameraAnimator?.cancel(); popAnimator.cancel(); pulseAnimator.cancel()
    }

    companion object {
        private fun polygon(pts: FloatArray): Path = Path().apply {
            moveTo(pts[0], pts[1])
            var i = 2
            while (i < pts.size) { lineTo(pts[i], pts[i + 1]); i += 2 }
            close()
        }

        private fun pointInPolygon(x: Float, y: Float, p: FloatArray): Boolean {
            var inside = false
            val n = p.size / 2
            var j = n - 1
            for (i in 0 until n) {
                val xi = p[i * 2]; val yi = p[i * 2 + 1]
                val xj = p[j * 2]; val yj = p[j * 2 + 1]
                if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) inside = !inside
                j = i
            }
            return inside
        }
    }
}
