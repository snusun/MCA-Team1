package com.example.aganada.wordbook

import android.content.Context
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding
import com.example.aganada.R
import android.widget.PopupWindow as PopupWindow 

class FlipCard private constructor(private val layout: GridLayout,
                                   private val imageView: ImageView,
                                   private val textView: TextView,
                                   private val duration: Long) {

    private val frameLayout = FrameLayout(layout.context)

    companion object {
        const val ANGLE_IMAGE_SHOW = 0f
        const val ANGLE_IMAGE_HIDE = 90f
        const val ANGLE_TEXT_SHOW = 360f
        const val ANGLE_TEXT_HIDE = 270f
        const val CARD_PADDING = 4.0f

        fun create(layout: GridLayout, imageId: Int, text: String, duration: Long = 300) : FlipCard {
            val imageView = ImageView(layout.context)
            val textView = TextView(layout.context)

            imageView.setImageResource(imageId)
            textView.text = text
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            textView.gravity = Gravity.CENTER

            return FlipCard(layout, imageView, textView, duration)
        }
    }

    init {
        val width = layout.width / layout.columnCount
        val height = width * 3 / 4
        frameLayout.setPadding(convertDpToPixel(CARD_PADDING, layout.context).toInt())
        frameLayout.layoutParams = LinearLayout.LayoutParams(width, height)

        imageView.setBackgroundResource(R.drawable.background_card)
        textView.setBackgroundResource(R.drawable.background_card)

        imageView.setOnClickListener { rotateImageToText() }
        textView.setOnClickListener { rotateTextToImage() }

        imageView.setOnLongClickListener { longClick() }
        textView.setOnLongClickListener { longClick() }

        setVisibility(showImage = true)
        imageView.rotationY = ANGLE_IMAGE_SHOW
        textView.rotationY = ANGLE_TEXT_HIDE

        frameLayout.addView(textView)
        frameLayout.addView(imageView)

        frameLayout.clipChildren = false
    }

    private fun longClick(): Boolean {
        detach()
        return false
    }

    private fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun setVisibility(showImage: Boolean) {
        when (showImage) {
            true -> {
                imageView.visibility = View.VISIBLE
                textView.visibility = View.INVISIBLE
            }
            false -> {
                imageView.visibility = View.INVISIBLE
                textView.visibility = View.VISIBLE
            }
        }
    }

    private fun rotateImageToText() {
        imageView.animate().rotationY(ANGLE_IMAGE_HIDE).setDuration(duration).withEndAction {
            setVisibility(showImage = false)
            textView.animate().rotationY(ANGLE_TEXT_SHOW).duration = duration
        }
    }

    private fun rotateTextToImage() {
        textView.animate().rotationY(ANGLE_TEXT_HIDE).setDuration(duration).withEndAction {
            setVisibility(showImage = true)
            imageView.animate().rotationY(ANGLE_IMAGE_SHOW).duration = duration
        }
    }

    fun detach() {
        layout.removeView(frameLayout)
    }

    fun attach() {
        layout.addView(frameLayout)
    }

}