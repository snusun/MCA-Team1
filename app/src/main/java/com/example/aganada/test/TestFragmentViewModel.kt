package com.example.aganada.test

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.example.aganada.R
import com.example.aganada.views.WordView.DrawMode
import java.io.File

class TestFragmentViewModel: ViewModel() {
    private val _drawMode: MutableLiveData<DrawMode> = MutableLiveData(DrawMode.PENCIL)
    val drawMode: LiveData<DrawMode> = _drawMode

    private val _photo: MutableLiveData<File> = MutableLiveData()
    val photo: LiveData<File> = _photo


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
}
