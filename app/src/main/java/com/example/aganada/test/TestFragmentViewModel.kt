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

    private val _checkResult: MutableLiveData<LearnFragmentViewModel.CheckResult> = MutableLiveData()
    val checkResult: LiveData<LearnFragmentViewModel.CheckResult> = _checkResult

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

    private lateinit var wordbook: List<File>
    private var index = 0;

    private fun setIndex(index: Int) {
        if (index >= 0 && index < wordbook.size) {
            this.index = index
            val file = wordbook[index]
            val label = PhotoFiles.getLabel(file.absolutePath)
            _photo.value = file
            _label.value = label
        }
    }

    fun loadPhoto(context: Context) {
        wordbook = PhotoFiles.getWordbook(context).shuffled()
        Log.d("wordbook", "wordbook size: ${wordbook.size} label: ${wordbook.firstOrNull()?.absolutePath?: ""}")
        setIndex(0)
    }

    fun onModeButtonClicked(view: View) {
        _drawMode.value = when (drawMode.value) {
            DrawMode.PENCIL -> DrawMode.ERASER
            DrawMode.ERASER -> DrawMode.PENCIL
            else -> DrawMode.PENCIL
        }
    }

    fun recognizeText(set: Collection<WordView.PathData>) {
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
        this.setIndex(index - 1)
    }

    fun getNext() {
        this.setIndex(index + 1)
    }

    companion object {
        const val TAG = "TestFragmentViewModel"
    }
}
