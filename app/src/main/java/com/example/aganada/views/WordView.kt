package com.example.aganada.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.graphics.PointF
import com.google.mlkit.vision.digitalink.Ink
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


class WordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    private val paint = Paint()
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var padding: Float = 0f
    private var heightSize: Int = 0
    private var widthSize: Int = 0
    private var eraseSize: Float = 0f
    private var lineWidth: Float = 0f

    var word = ""
    set(value) {
        field = value
        invalidate()
    }

    private val drawOpStack = mutableListOf<DrawOp>()
    private val undoStack = mutableListOf<DrawOp>()
    private var removingList: MutableList<PathData>? = null
    val pathSet = mutableSetOf<PathData>()

    var drawMode: DrawMode = DrawMode.PENCIL

    private var path: PathData? = null
    private var eraserPath: Path? = null
    private var eraserPoint: PointF? = null

    fun unDo() {
        val op = drawOpStack.removeLastOrNull()
        op?.let{
            undoStack.add(op)
            when (op.type) {
                DrawOpType.PENCIL -> pathSet.removeAll(op.pathList)
                DrawOpType.ERASER -> pathSet.addAll(op.pathList)
            }
        }
        invalidate()
    }

    fun reDo() {
        val op = undoStack.removeLastOrNull()
        op?.let{
            drawOpStack.add(op)
            when (op.type) {
                DrawOpType.PENCIL -> pathSet.addAll(op.pathList)
                DrawOpType.ERASER -> pathSet.removeAll(op.pathList)
            }
        }
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (drawMode == DrawMode.PENCIL) {
            eraserPoint = null
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    path = PathData(event.x, event.y).also {
                        drawOpStack.add(DrawOp(DrawOpType.PENCIL, listOf(it)))
                        pathSet.add(it)
                        undoStack.clear()
                    }

                MotionEvent.ACTION_MOVE -> {
                    path?.lineTo(event.x, event.y)
                }

                else -> {
                }
            }
        } else if (drawMode == DrawMode.ERASER) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    eraserPath = Path()
                    eraserPath?.fillType = Path.FillType.WINDING
                    eraserPath?.moveTo(event.x, event.y)
                    eraserPoint = PointF(event.x, event.y)
                    undoStack.clear()
                    removingList = mutableListOf()
                }

                MotionEvent.ACTION_MOVE -> {
                    val eraserPath = eraserPath?: return false
                    eraserPath.apply {
                        lineTo(event.x, event.y)
                        addCircle(event.x, event.y, eraseSize, Path.Direction.CW)
                        val removeList = pathSet.filter { p ->
                            val interPath = Path()
                            interPath.op(p.roundPath, eraserPath, Path.Op.INTERSECT)
                            return@filter !interPath.isEmpty
                        }
                        pathSet.removeAll(removeList)
                        removingList?.addAll(removeList)
                        reset()
                        moveTo(event.x, event.y)
                    }
                    eraserPoint?.set(event.x, event.y)
                }

                MotionEvent.ACTION_UP -> {
                    val eraserPath = eraserPath ?: return true
                    val removeList = pathSet.filter { p ->
                        val interPath = Path()
                        interPath.op(p.roundPath, eraserPath, Path.Op.INTERSECT)
                        return@filter !interPath.isEmpty
                    }
                    eraserPoint = null
                    this.eraserPath = null
                    this.pathSet.removeAll(removeList)
                    val removingList = removingList?: return true
                    removingList.addAll(removeList)
                    if (removeList.isNotEmpty()) {
                        drawOpStack.add(DrawOp(DrawOpType.ERASER, removingList))
                    }
                }
                else -> {
                }
            }
        }
        invalidate()
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        heightSize = MeasureSpec.getSize(heightMeasureSpec)
        centerX = (widthSize / 2).toFloat()
        centerY = (heightSize / 2).toFloat()
        padding = convertDpToPixel(16f, context)
        eraseSize = convertDpToPixel(8f, context)
        lineWidth = convertDpToPixel(4f, context)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?: return
//        Log.d("WordView", "onDraw $centerX $centerY")

        /* draw word */
        run {
            paint.color = Color.parseColor("#22000000")
            paint.style = Paint.Style.FILL
            paint.textSize = heightSize - padding * 2
            val maxChar = "N"
            val textX = centerX - (paint.measureText(word) / 2)
            val textY = centerY + paint.measureText(maxChar) / 2
            canvas.drawText(word, textX, textY, paint)

            paint.color = Color.parseColor("#44000000")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = lineWidth
            canvas.drawText(word, textX, textY, paint)
        }

        /* draw line */
        run {
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = lineWidth

            pathSet.forEach { p ->
                canvas.drawPath(p.rawPath, paint)
            }
        }

        /* draw Eraser */
        run {
            if (drawMode == DrawMode.ERASER) {
                eraserPoint?.let{
                    paint.color = Color.argb(33, 0, 0, 0)
                    paint.strokeWidth = 1f
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(it.x, it.y, eraseSize, paint)
                    eraserPath?.let { it1 -> canvas.drawPath(it1, paint) }
                }
            }
        }
    }

    inner class PathData(x: Float, y: Float) {
        val rawPath: Path = Path()
        private val _roundPath: Path = Path()
        val roundPath: Path
            get() { if (!isMade) makePath(); return _roundPath }

        private val _inkPointList: MutableList<Ink.Point> = mutableListOf()
        val inkPointList: List<Ink.Point>
            get() { if (!isMade) makePath(); return _inkPointList }

        private var isMade = false

        private val startPoint: PointF = PointF()
        private val matrix: Matrix = Matrix()

        init {
            rawPath.moveTo(x, y)
            _inkPointList.add(Ink.Point.create(x, y, System.currentTimeMillis()))
            startPoint.set(x, y)
        }

        fun lineTo(x: Float, y: Float) {
            _inkPointList.add(Ink.Point.create(x, y, System.currentTimeMillis()))
            rawPath.lineTo(x, y)
        }

        private fun makePath() {
            val prevPoint = PointF(startPoint.x, startPoint.y)
            for (point in _inkPointList) {
                val degree = angleOf(prevPoint, PointF(point.x, point.y))
                val midX = (prevPoint.x + point.x) / 2
                val midY = (prevPoint.y + point.y) / 2
                val halfWidth = convertDpToPixel(2f, context)
                val halfLength = sqrt((prevPoint.x - point.x).pow(2) + (prevPoint.y - point.y).pow(2)) / 2
                val points = listOf(
                    midX - halfLength, midY + halfWidth,
                    midX + halfLength, midY + halfWidth,
                    midX + halfLength, midY - halfWidth,
                    midX - halfLength, midY - halfWidth
                ).toFloatArray()
                matrix.reset()
                matrix.setRotate(-degree.toFloat(), midX, midY)
                matrix.mapPoints(points)
                _roundPath.moveTo(points[0], points[1])
                _roundPath.lineTo(points[2], points[3])
                _roundPath.lineTo(points[4], points[5])
                _roundPath.lineTo(points[6], points[7])
                prevPoint.set(point.x, point.y)
            }
            isMade = true
        }
    }

    data class DrawOp(
        val type: DrawOpType,
        var pathList: List<PathData>
    )

    enum class DrawMode {
        ERASER, PENCIL
    }

    enum class DrawOpType {
        ERASER, PENCIL
    }

    companion object {
        private fun convertDpToPixel(dp: Float, context: Context): Float {
            return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun angleOf(p1: PointF, p2: PointF): Double {
            val deltaY = (p1.y - p2.y).toDouble()
            val deltaX = (p2.x - p1.x).toDouble()
            val result = Math.toDegrees(atan2(deltaY, deltaX))
            return if (result < 0) 360.0 + result else result
        }
    }
}