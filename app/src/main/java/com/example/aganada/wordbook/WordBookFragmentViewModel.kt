package com.example.aganada.wordbook

import android.util.Log
import androidx.lifecycle.ViewModel
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Layout
import android.widget.GridLayout
import java.io.File
import java.nio.file.Files

class WordBookFragmentViewModel: ViewModel()  {

    private fun getOutputDirectory(dataDir: File): File {
        val dir = File(dataDir.canonicalPath + File.separator + "files/tmp")
        if (!dir.exists()) {
            dir.mkdir()
            Log.d("OUTPUTDIR", "make directory")
        }
        Log.d("OUTPUTDIR", dir.toString())
        return dir
    }

    fun loadImages(layout: GridLayout, dataDir: File?) {
        if (dataDir == null) return

        val dir = getOutputDirectory(dataDir)
        dir.walk()
            .filter { item -> item.isFile }
            .filter { item -> item.toString().endsWith(".jpg") }
            .forEach {
                val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                FlipCard.create(layout, bitmap, "Test")
            }
    }

    fun printListDir(dataDir: File?) {
        if (dataDir == null) {
            Log.d("JHTEST", "Null DataDir")
            return
        }
        val dir = getOutputDirectory(dataDir)
        Log.d("JHTEST", dir.toString())
        val paths = dir.walk()
            .filter { item -> item.isFile }
            .filter { item -> item.toString().endsWith(".jpg") }

        println(paths)
    }
}