package com.example.aganada.test

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aganada.PhotoFiles
import com.example.aganada.learn.LearnFragmentViewModel
import com.example.aganada.views.InkManager
import com.example.aganada.views.WordView
import com.example.aganada.views.WordView.DrawMode
import com.google.mlkit.vision.digitalink.Ink
import java.io.File

class TestFragmentViewModel: ViewModel() {
    private val _drawMode: MutableLiveData<DrawMode> = MutableLiveData(DrawMode.PENCIL)
    val drawMode: LiveData<DrawMode> = _drawMode

    private val _photo: MutableLiveData<File> = MutableLiveData()
    val photo: LiveData<File> = _photo

    private val _label: MutableLiveData<String> = MutableLiveData()
    val label: LiveData<String> = _label

    private val _index: MutableLiveData<Int> = MutableLiveData()
    val index: LiveData<Int> = _index

    private val _wordbook: MutableLiveData<List<File>> = MutableLiveData(listOf())
    val wordbook: LiveData<List<File>> = _wordbook

    private val _checkResult: MutableLiveData<LearnFragmentViewModel.CheckResult> = MutableLiveData()
    val checkResult: LiveData<LearnFragmentViewModel.CheckResult> = _checkResult

    private val _shuffled: MutableLiveData<Boolean> = MutableLiveData(false)
    val shuffled: LiveData<Boolean> = _shuffled

    private val inkManager: InkManager = InkManager().also {
        it.setActiveModel("ko")
        it.download()
        it.setOnResultListener(object : InkManager.OnResultListener{
            override fun onSuccessListener(result: String) {
                onRecognitionResultOut(result)
            }

            override fun onFailureListener() {
                Log.e(TAG, "Failed to recognize text")
            }
        })
    }

    private fun setIndex(index: Int) {
        if (index >= 0 && index < wordbook.value!!.size) {
            val file = wordbook.value!![index]
            val label = PhotoFiles.getLabel(file.absolutePath)
            _index.value = index
            _photo.value = file
            _label.value = label
        }
    }

    fun loadPhoto(context: Context) {
        val wordbook = PhotoFiles.getWordbook(context)
        Log.d("wordbook", "wordbook size: ${wordbook.size} label: ${wordbook.firstOrNull()?.absolutePath?: ""}")
        _shuffled.value = false
        _wordbook.value = wordbook
        setIndex(0)
    }

    private fun sortPhoto(decent: Boolean) {
        val sorted = PhotoFiles.sortWordbook(wordbook.value?: listOf())
        _wordbook.value = if (decent) sorted.reversed() else sorted
        _shuffled.value = false
    }

    private fun shufflePhoto() {
        val shuffled = wordbook.value?.shuffled() ?: listOf()
        _wordbook.value = shuffled
        _shuffled.value = true
    }

    fun onModeButtonClicked(view: View) {
        _drawMode.value = when (drawMode.value) {
            DrawMode.PENCIL -> DrawMode.ERASER
            DrawMode.ERASER -> DrawMode.PENCIL
            else -> DrawMode.PENCIL
        }
    }

    fun recognizeText(set: Collection<WordView.PathData>) {
        if (set.isEmpty()) {
            return
        }
        if (checkResult.value?.working == true) {
            return
        }
        _checkResult.value = LearnFragmentViewModel.CheckResult(working = true)
        val inkBuilder = Ink.builder()
        for (data in set) {
            val strokeBuilder = Ink.Stroke.builder()
            for (point in data.inkPointList) {
                strokeBuilder.addPoint(point)
            }
            inkBuilder.addStroke(strokeBuilder.build())
        }
        inkManager.recognize(inkBuilder)
    }

    fun onRecognitionResultOut(result: String) {
        fun String.removeWhitespaces() = replace(" ", "")
        _checkResult.value = LearnFragmentViewModel.CheckResult(
            correct = result.removeWhitespaces() == label.value?.removeWhitespaces(),
            label = label.value ?: "",
            answer = result,
        )
    }

    fun getPrev() {
        this.setIndex(index.value!! - 1)
    }

    fun getNext() {
        this.setIndex(index.value!! + 1)
    }

    fun onShuffleClicked() {
        if (shuffled.value == true) {
            this.sortPhoto(false)
        } else {
            this.shufflePhoto()
        }
        setIndex(0)
    }

    companion object {
        const val TAG = "TestFragmentViewModel"
    }
}
