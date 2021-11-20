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
import java.util.regex.Matcher
import java.util.regex.Pattern

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
        dataDir?: return
        val dir = getOutputDirectory(dataDir)
        Log.d("JHTEST", dir.absolutePath)
        dir.walk()
            .filter { item -> item.isFile }
            .filter { item -> item.toString().endsWith(".jpeg") }
            .forEach {
                Log.d("JHTEST", it.absolutePath)
                val label = parseLabel(it.absolutePath)
                if (label != null) FlipCard.create(layout, it.absolutePath, label).attach()
            }
    }

    private fun parseLabel(filename: String): String? {
        val pattern: Pattern = Pattern.compile("^.+tmp/(.+)_.+\\.jpeg$")
        val matches: Matcher = pattern.matcher(filename)
        if (matches.matches()) return matches.group(1)

        Log.v("JHTEST", "$filename no Label found.")
        return null
    }

}