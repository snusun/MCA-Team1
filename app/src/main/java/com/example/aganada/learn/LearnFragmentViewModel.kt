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

class LearnFragmentViewModel: ViewModel() {
    private val _drawMode: MutableLiveData<DrawMode> = MutableLiveData(DrawMode.PENCIL)
    val drawMode: LiveData<DrawMode> = _drawMode

    private val _photo: MutableLiveData<File> = MutableLiveData()
    val photo: LiveData<File> = _photo

    private val _recognitionResult: MutableLiveData<String> = MutableLiveData()
    val recognitionResult: LiveData<String> = _recognitionResult

    private val inkManager: InkManager = InkManager().also {
        it.setActiveModel("ko")
        it.download()
        it.setOnResultListener(object : InkManager.OnResultListener{
            override fun onSuccessListener(result: String) {
                _recognitionResult.value = result
            }

            override fun onFailureListener() {
                Log.e(TestFragmentViewModel.TAG, "Failed to recognize text")
            }
        })
    }

    fun loadPhoto(context: Context) {
        // TODO ("Load Photo File")
//        _photo.value = photo_file
    }

    fun onWordbookButtonClicked(view: View) {
        view.findNavController().navigate(
            R.id.action_learnFragment_to_wordBookFragment)
    }

    fun onCameraButtonClicked(view: View) {
        view.findNavController().navigate(
            R.id.action_learnFragment_to_cameraFragment2)
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
}
