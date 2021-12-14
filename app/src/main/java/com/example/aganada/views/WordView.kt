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
import com.example.aganada.R
import com.google.mlkit.vision.digitalink.Ink
import kr.bydelta.koala.dissembleHangul
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


class WordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    private val paint = Paint().also {
        it.typeface = context.resources.getFont(R.font.font)
    }
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var padding: Float = 0f
    private var heightSize: Int = 0
    private var widthSize: Int = 0
    private var eraseSize: Float = 0f
    private var lineWidth: Float = 0f
    private var fontSize: Float = 0f
    private var textBounds: Rect = Rect()

    var word = ""
    set(value) {
        field = value
        measureFontSize()
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

    private val onset = listOf(Char(0x1100), Char(0x1101), Char(0x1102),
        Char(0x1103), Char(0x1104), Char(0x1105), Char(0x1106),
        Char(0x1107), Char(0x1108), Char(0x1109), Char(0x110A),
        Char(0x110B), Char(0x110C), Char(0x110D), Char(0x110E),
        Char(0x110F), Char(0x1110), Char(0x1111), Char(0x1112))
    //0x1161..0x1175
    private val nucleus = arrayListOf(Char(0x1161), Char(0x1162), Char(0x1163),
        Char(0x1164), Char(0x1165), Char(0x1166), Char(0x1167),
        Char(0x1168), Char(0x1169), Char(0x116A), Char(0x116B),
        Char(0x116C), Char(0x116D), Char(0x116E), Char(0x116F),
        Char(0x1170), Char(0x1171), Char(0x1172), Char(0x1173),
        Char(0x1174), Char(0x1175))

    private val horizonNucleus = arrayListOf(Char(0x1169), Char(0x116D),
        Char(0x116E), Char(0x1172), Char(0x1173))
    //0x11A8..0x11C2
    private val coda = arrayListOf(null, Char(0x11A8), Char(0x11A9), Char(0x11AA),
        Char(0x11AB), Char(0x11AC), Char(0x11AD), Char(0x11AE),
        Char(0x11AF), Char(0x11B0), Char(0x11B1), Char(0x11B2),
        Char(0x11B3), Char(0x11B4), Char(0x11B5), Char(0x11B6),
        Char(0x11B7), Char(0x11B8), Char(0x11B9), Char(0x11BA),
        Char(0x11BB), Char(0x11BC), Char(0x11BD), Char(0x11BE),
        Char(0x11BF), Char(0x11C0), Char(0x11C1), Char(0x11C2))

    // 초성, 중성, 종성 획수
    private val onsetStrokes = listOf(1, 2, 1, 2, 4, 3, 3, 4, 8, 2, 4, 1, 2, 4, 3, 2, 3, 4, 3)
    private val nucleusStrokes = arrayListOf(2, 3, 3, 4, 2, 3, 3, 4, 2, 4, 5, 3, 3, 2, 4, 5, 3, 3, 1, 2, 1)
    private val codaStrokes = arrayListOf(0, 1, 2, 3, 1, 3, 4, 2, 3, 4, 6, 7, 5, 6, 7, 6, 3, 4, 6, 2, 4, 1, 2, 3, 2, 3, 4, 3)

    init {
        for (c in 'A'..'Z') {
            Log.d("charsize", paint.measureText(c.toString()).toString())

        }
    }

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

    fun clear() {
        drawOpStack.clear()
        undoStack.clear()
        removingList = null
        pathSet.clear()
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
                    val removingList = removingList?: mutableListOf()
                    removingList.addAll(removeList)
                    if (removingList.isNotEmpty()) {
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

    private fun measureFontSize() {
        paint.textSize = heightSize - padding * 2
        if (paint.measureText(word) >( widthSize - padding * 2)) {
            paint.textSize =
                ( widthSize - padding * 2) / paint.measureText(word) * paint.textSize
        }
        fontSize = paint.textSize

        paint.getTextBounds(word, 0, word.length, textBounds)
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

        measureFontSize()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?: return
//        Log.d("WordView", "onDraw $centerX $centerY")

        /* draw word */
        run {
            val fm = paint.fontMetrics
            val textWidth = paint.measureText(word)
            val textHeight = (fm.descent - fm.ascent) / 2
            val textSpace = (fm.bottom - fm.top)
            //Log.d("sunyoung", width.toString())

            paint.color = Color.parseColor("#22000000")
            paint.style = Paint.Style.FILL
            paint.textSize = (fontSize * 0.8).toFloat()
            paint.textAlign = Paint.Align.CENTER

            val textX = (width shr 1).toFloat()
            val textY = centerY + textHeight / 2
            canvas.drawText(word, textX, textY, paint)

            paint.color = Color.parseColor("#44000000")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = lineWidth
            print(fm)
            canvas.drawText(word, textX, textY, paint)

            Log.d("sunyoung", "$textX $textWidth")
            val stroke = strokes(word)
            for(i in word.indices){
                println()
                println(stroke[i])
            }
            paint.textSize = 70f
            canvas.drawText(
                "1",
                (textX - textWidth / 2), (centerY - textSpace * 0.5).toFloat(), paint
            )
            canvas.drawText("2", textX, (centerY + textSpace * 0.5).toFloat(), paint)
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

    fun strokes(label: String): IntArray {
        var strList = IntArray(label.length) { 0 }
        for (i in label.indices) {
            if (label[i].dissembleHangul() == null) {
                strList[i] = 0
                continue
            }
            val char = label[i].dissembleHangul()?.toList() as List<*>
            if(char[2]==null){
                if(char[1] in horizonNucleus) {
                    strList[i] = 1
                } else {
                    strList[i] = 2
                }
            } else {
                if(char[1] in horizonNucleus) {
                    strList[i] = 3
                } else {
                    strList[i] = 4
                }
            }
        }
        return strList
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
