package com.example.aganada.learn

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aganada.PhotoFiles
import com.example.aganada.test.TestFragmentViewModel
import com.example.aganada.views.InkManager
import com.example.aganada.views.WordView
import com.example.aganada.views.WordView.DrawMode
import com.google.mlkit.vision.digitalink.Ink
import java.io.File

class LearnFragmentViewModel: ViewModel() {
    private val _drawMode: MutableLiveData<DrawMode> = MutableLiveData(DrawMode.PENCIL)
    val drawMode: LiveData<DrawMode> = _drawMode

    private val _photo: MutableLiveData<File> = MutableLiveData()
    val photo: LiveData<File> = _photo

    private val _label: MutableLiveData<String> = MutableLiveData()
    val label: LiveData<String> = _label

    private val _checkResult: MutableLiveData<CheckResult> = MutableLiveData()
    val checkResult: LiveData<CheckResult> = _checkResult

    private val inkManager: InkManager = InkManager().also {
        it.setActiveModel("ko")
        it.download()
        it.setOnResultListener(object : InkManager.OnResultListener{
            override fun onSuccessListener(result: String) {
                onRecognitionResultOut(result)
            }

            override fun onFailureListener() {
                Log.e(TestFragmentViewModel.TAG, "Failed to recognize text")
            }
        })
    }

    private fun onRecognitionResultOut(result: String) {
        fun String.removeWhitespaces() = replace(" ", "")
        _checkResult.value = CheckResult(
            correct = result.removeWhitespaces() == label.value?.removeWhitespaces(),
            label = label.value?: "",
            answer = result,
        )
    }

    fun loadPhoto(filename: String) {
        val label = PhotoFiles.getLabel(filename)
        val file = File(filename)
        if (label.isNotBlank() && file.exists()) {
            _photo.value = file
            _label.value = label
        } else {
            Log.v("JONGSUN", "$filename no Label found.")
        }
    }

    fun onModeButtonClicked(view: View) {
        _drawMode.value = when (drawMode.value) {
            DrawMode.PENCIL -> DrawMode.ERASER
            DrawMode.ERASER -> DrawMode.PENCIL
            else -> DrawMode.PENCIL
        }
    }

    fun recognizeText(set: Collection<WordView.PathData>) {
        if (checkResult.value?.working == true) {
            return
        }
        _checkResult.value = CheckResult(working = true)
        inkManager.getStatus()
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

    fun movePhotoToWordBook() {
        val tempFile = this.photo.value ?: return
        PhotoFiles.moveTempToWordbook(tempFile)
    }

    data class CheckResult(
        val working: Boolean = false,
        val correct: Boolean = false,
        val label: String = "",
        val answer: String = ""
    )
}
