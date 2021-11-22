package com.example.aganada.wordbook

import androidx.lifecycle.ViewModel
import android.content.Context
import android.widget.GridLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.aganada.PhotoFiles
import java.io.File

class WordBookFragmentViewModel: ViewModel()  {

    private val _wordbook: MutableLiveData<List<File>> = MutableLiveData(listOf())
    val wordbook: LiveData<List<File>> = _wordbook

    fun addImage(gridLayout: GridLayout, file: File) {
        val layout = FlipCardLayout.create(gridLayout.context, file)
        layout.load()

        gridLayout.post {
            layout.resize(gridLayout.measuredWidth, gridLayout.columnCount)
            gridLayout.addView(layout)
        }
    }

    fun loadImages(context: Context) {
        _wordbook.value = PhotoFiles.getWordbook(context)
    }

}