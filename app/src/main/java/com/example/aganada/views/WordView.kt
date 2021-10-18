package com.example.aganada.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

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

    var word = ""
    set(value) {
        field = value
        invalidate()
    }

    private val drawOpStack = mutableListOf<DrawOp>()
    private val undoStack = mutableListOf<DrawOp>()

    private val pathSet = mutableSetOf<Path>()

    private var removingList: MutableList<Path>? = null

    var drawMode: DrawMode = DrawMode.PENCIL

    private var path: Path? = null

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

    private fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (drawMode == DrawMode.PENCIL) {
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    path = Path().also {
                        it.moveTo(event.x, event.y)
                        drawOpStack.add(DrawOp(DrawOpType.PENCIL, listOf(it)))
                        pathSet.add(it)
                        undoStack.clear()
                    }
                MotionEvent.ACTION_MOVE ->
                    path?.lineTo(event.x, event.y)
                else -> {
                }
            }
        } else if (drawMode == DrawMode.ERASER) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    path = Path().also {
                        it.moveTo(event.x, event.y)
                    }
                    undoStack.clear()
                    removingList = mutableListOf()
                }

                MotionEvent.ACTION_MOVE -> {
                    path?.lineTo(event.x, event.y)
                    val erasePath = path ?: return true
                    val removeList = pathSet.filter { p ->
                        val interPath = Path()
                        interPath.op(p, erasePath, Path.Op.INTERSECT)
                        return@filter !interPath.isEmpty
                    }
                    this.pathSet.removeAll(removeList)
                    removingList?.addAll(removeList)
                }

                MotionEvent.ACTION_UP -> {
                    val erasePath = path ?: return true
                    val removeList = pathSet.filter { p ->
                        val interPath = Path()
                        interPath.op(p, erasePath, Path.Op.INTERSECT)
                        return@filter !interPath.isEmpty
                    }
                    this.pathSet.removeAll(removeList)
                    val removingList = removingList?: return true
                    removingList.addAll(removeList)
                    drawOpStack.add(DrawOp(DrawOpType.ERASER, removingList))
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
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        heightSize = MeasureSpec.getSize(heightMeasureSpec)
        centerX = (widthSize / 2).toFloat()
        centerY = (heightSize / 2).toFloat()
        padding = convertDpToPixel(16f, context)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?: return
        Log.d("WordView", "onDraw $centerX $centerY")

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
            paint.strokeWidth = 10f
            canvas.drawText(word, textX, textY, paint)
        }

        /* draw line */
        run {
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 10f

            pathSet.forEach { p ->
                canvas.drawPath(p, paint)
            }

        }
    }

    data class DrawOp(
        val type: DrawOpType,
        var pathList: List<Path>
    )

    enum class DrawMode {
        ERASER, PENCIL
    }

    enum class DrawOpType {
        ERASER, PENCIL
    }
}