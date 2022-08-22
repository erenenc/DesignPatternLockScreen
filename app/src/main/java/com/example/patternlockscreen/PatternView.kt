package com.example.patternlockscreen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import java.lang.Math.abs

class PatternView: View {

    companion object {
        private const val TAG = "PatternView"
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    private var density: Float = context.resources.displayMetrics.density
    private val paint = Paint()

    private val pointPositions = mutableListOf<PatternPoint>()
    private val patternPath = mutableListOf<PatternPoint>()

    private var touchedX: Float = 0.0f
    private var touchedY: Float = 0.0f

    var onPatternChanged: ((patternstring: String) -> Unit)? = null
    var onCheckPattern: ((patternstring: String) -> Unit)? = null

    init {

        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        paint.isAntiAlias = true

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = measuredWidth
        var height = measuredHeight

        width = resolveSize(width, widthMeasureSpec)
        height = resolveSize(height, heightMeasureSpec)

        setMeasuredDimension(width, height)

        // init pattern points data
        initPatternPoints(width*0.5f, height*0.5f, 8*density) // radius of point is 8dp
    }

    private fun initPatternPoints(centerX: Float, centerY: Float, radius: Float) {

        pointPositions.clear()

        // row 1
        var point = PatternPoint(centerX*0.5f, centerY*0.5f, radius, "a") // 11
        pointPositions.add(point)

        point = PatternPoint(centerX, centerY*0.5f, radius, "b") // 12
        pointPositions.add(point)

        point = PatternPoint(centerX*0.5f + centerX, centerY*0.5f, radius, "c") // 13
        pointPositions.add(point)


        // row 2
        point = PatternPoint(centerX*0.5f, centerY, radius, "d") // 21
        pointPositions.add(point)

        point = PatternPoint(centerX, centerY, radius, "e") // 22
        pointPositions.add(point)

        point = PatternPoint(centerX*0.5f + centerX, centerY, radius, "f") // 23
        pointPositions.add(point)


        // row 3
        point = PatternPoint(centerX*0.5f, centerY*0.5f + centerY, radius, "g") // 31
        pointPositions.add(point)

        point = PatternPoint(centerX, centerY*0.5f + centerY, radius, "h") // 32
        pointPositions.add(point)

        point = PatternPoint(centerX*0.5f + centerX, centerY*0.5f + centerY, radius, "i") // 33
        pointPositions.add(point)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // draw background
        drawBackground(canvas)
        drawPoints(canvas)
        drawPatternPath(canvas)
        drawPatternGuideLine(canvas)
    }

    private fun drawBackground(canvas: Canvas?) {
        canvas?.drawColor(Color.parseColor("#666666"))
    }

    private fun drawPoints(canvas: Canvas?) {
        paint.color = Color.parseColor("#ffffff")
        paint.strokeWidth = 8.0f

        for (point in pointPositions) {
            paint.style = if (point.isSelected) {
                Paint.Style.FILL
            } else {
                Paint.Style.STROKE
            }
            canvas?.drawCircle(point.x, point.y, point.r, paint)
        }
    }

    private fun drawPatternPath(canvas: Canvas?) {
        paint.color = Color.WHITE
        paint.strokeWidth = 20f

        // connect all the points in the path list
        if (patternPath.size < 2) {
            return
        }
        if (patternPath.size > pointPositions.size) {
            return
        }

        for (i in 1 until patternPath.size) {
            canvas?.drawLine(
                patternPath[i-1].x, patternPath[i-1].y,
                patternPath[i].x, patternPath[i].y,
                paint
            )
        }
    }

    private fun drawPatternGuideLine(canvas: Canvas?) {
        paint.color = Color.BLACK
        paint.strokeWidth = 20f

        if (patternPath.size == 0) {
            return
        }
        if (patternPath.size >= pointPositions.size) {
            return
        }

        // from last selected point to touched position
        canvas?.drawLine(
            patternPath[patternPath.size -1].x,
            patternPath[patternPath.size -1].y,
            touchedX,
            touchedY,
            paint
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when(event?.action) {

            MotionEvent.ACTION_DOWN -> {

            }

            MotionEvent.ACTION_MOVE -> {

                touchedX = event.x
                touchedY = event.y

                for (point in pointPositions) {
                    // if your touched position is near the point
                    if (kotlin.math.abs(event.x - point.x) < point.r*1.5 &&
                        kotlin.math.abs(event.y - point.y) < point.r*1.5) {

                        if (!point.isSelected) {
                            // point can be selected only one time
                            point.isSelected = true

                            // add the touched point to path list
                            patternPath.add(point)

                            // if selected, update the pattern string UI
                            onPatternChangedListener()

                        }
                    }
                }

                // call onDraw to update UI
                invalidate()

            }

            MotionEvent.ACTION_UP -> {

                // check pattern before clear pattern path
                onCheckPatternListener()

                // clear pattern path when your finger leaves the screen
                clearPatternPath()

                invalidate()

            }


        }

        return true

    }

    private fun onPatternChangedListener() {
        onPatternChanged?.let { onPatternChanged ->
            var patternString = ""

            for (point in patternPath) {
                if (point.isSelected) {
                    patternString += point.pattern
                }
            }
            onPatternChanged(patternString)
        }
    }

    private fun onCheckPatternListener() {
        onCheckPattern?.let { onCheckPattern ->
            var patternString = ""

            for (point in patternPath) {
                if (point.isSelected) {
                    patternString += point.pattern
                }
            }
            onCheckPattern(patternString)
        }
    }

    private fun clearPatternPath() {

        for (point in pointPositions) {
            point.isSelected = false
        }

        touchedX = 0.0f
        touchedY = 0.0f

        patternPath.clear()

    }

    data class PatternPoint(
        val x: Float = 0.0f,
        val y: Float = 0.0f,
        val r: Float = 0.0f, // radius
        val pattern: String = ""
    ) {
        var isSelected = false
    }

}