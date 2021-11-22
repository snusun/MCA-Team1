package com.example.aganada.wordbook

import android.content.Context
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup

import android.widget.*
import androidx.core.view.setPadding
import com.example.aganada.PhotoFiles
import com.example.aganada.R
import java.io.File
import com.bumptech.glide.Glide


class FlipCardLayout private constructor(context: Context,
                                         private val imageView: ImageView,
                                         private val textView: TextView,
                                         private val file: File,
                                         private val duration: Long) : FrameLayout(context) {

    companion object {
        const val ANGLE_IMAGE_SHOW = 0f
        const val ANGLE_IMAGE_HIDE = 90f
        const val ANGLE_TEXT_SHOW = 360f
        const val ANGLE_TEXT_HIDE = 270f
        const val CARD_PADDING = 4.0f

        fun create(context: Context, file: File): FlipCardLayout {
            val imageView = ImageView(context)
            val textView = TextView(context)

            imageView.clipToOutline = true
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            textView.gravity = Gravity.CENTER

            val layout = FlipCardLayout(context, imageView, textView, file, 300)
            layout.addView(imageView)
            layout.addView(textView)
            layout.clipChildren = false

            return layout
        }
    }

    init {

        imageView.setBackgroundResource(R.drawable.background_card_image)
        textView.setBackgroundResource(R.drawable.background_card)

        imageView.setOnClickListener { rotateImageToText() }
        textView.setOnClickListener { rotateTextToImage() }

        imageView.setOnLongClickListener { longClick() }
        textView.setOnLongClickListener { longClick() }

        setVisibility(showImage = true)
        imageView.rotationY = ANGLE_IMAGE_SHOW
        textView.rotationY = ANGLE_TEXT_HIDE

    }

    fun resize(parentWidth: Int, columnCount: Int) {
        val width = parentWidth / columnCount
        val height = width * 3 / 4

        this.setPadding(convertDpToPixel(CARD_PADDING).toInt())
        this.layoutParams = LinearLayout.LayoutParams(width, height)
    }

    fun load() {
        Glide.with(context).load(file).into(imageView)
        textView.text = PhotoFiles.getLabel(file.absolutePath)
    }

    private fun delete() {
        (parent as? ViewGroup)!!.removeView(this)
    }

    private fun longClick(): Boolean {
        file.delete()
        delete()
        return false
    }

    private fun convertDpToPixel(dp: Float): Float {
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

}
