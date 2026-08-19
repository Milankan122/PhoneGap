package com.example.phoneassistant

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

class JoystickView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    var onMove: ((x: Float, y: Float) -> Unit)? = null
    private val paint = Paint().apply { color = Color.DKGRAY; isAntiAlias = true }
    private val knobPaint = Paint().apply { color = Color.RED; isAntiAlias = true }
    private var knobX = 0f
    private var knobY = 0f
    private var radius = 0f
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2f * 0.8f
        knobX = w / 2f
        knobY = h / 2f
    }
    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
        canvas.drawCircle(knobX, knobY, radius * 0.25f, knobPaint)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val cx = width / 2f
                val cy = height / 2f
                var dx = event.x - cx
                var dy = event.y - cy
                val dist = hypot(dx, dy)
                if (dist > radius) {
                    val scale = radius / dist
                    dx *= scale
                    dy *= scale
                }
                knobX = cx + dx
                knobY = cy + dy
                invalidate()
                val nx = dx / radius
                val ny = -dy / radius
                onMove?.invoke(nx.coerceIn(-1f,1f), ny.coerceIn(-1f,1f))
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                knobX = width / 2f
                knobY = height / 2f
                invalidate()
                onMove?.invoke(0f, 0f)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
