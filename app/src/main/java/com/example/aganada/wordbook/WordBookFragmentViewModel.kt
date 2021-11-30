package com.example.aganada.wordbook

import androidx.lifecycle.ViewModel
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.aganada.PhotoFiles
import java.io.File

class WordBookFragmentViewModel: ViewModel()  {

    private val _wordbook: MutableLiveData<List<File>> = MutableLiveData(listOf())
    val wordbook: LiveData<List<File>> = _wordbook

    fun loadImages(context: Context) {
        _wordbook.value = PhotoFiles.getWordbook(context)
    }

}