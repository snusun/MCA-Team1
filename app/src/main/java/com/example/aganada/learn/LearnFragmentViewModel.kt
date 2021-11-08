package com.example.aganada.learn

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.example.aganada.R
import com.example.aganada.test.TestFragmentViewModel
import com.example.aganada.views.InkManager
import com.example.aganada.views.WordView
import com.example.aganada.views.WordView.DrawMode
import com.google.mlkit.vision.digitalink.Ink
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern

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

    fun loadPhoto(filename: String) {
        val pattern: Pattern = Pattern.compile("^.+tmp/(.+)_.+\\.jpeg$")
        val matches: Matcher = pattern.matcher(filename)
        if (matches.matches()) {
            val file = File(filename)
            val label = matches.group(1)
            if (file.exists()) {
                _photo.value = File(filename)
                _label.value = label
            }
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
        _checkResult.value = CheckResult(
            correct = result.removeWhitespaces() == label.value?.removeWhitespaces(),
            label = label.value?: "",
            answer = result,
        )
    }

    data class CheckResult(val correct: Boolean, val label: String, val answer: String)
}
