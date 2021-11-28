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
import kr.bydelta.koala.dissembleHangul
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

    // 초성, 중성, 종성
    private val onset = listOf(Char(0x1100), Char(0x1101), Char(0x1102),
        Char(0x1103), Char(0x1104), Char(0x1105), Char(0x1106),
        Char(0x1107), Char(0x1108), Char(0x1109), Char(0x110A),
        Char(0x110B), Char(0x110C), Char(0x110D), Char(0x110E),
        Char(0x110F), Char(0x1110), Char(0x1111), Char(0x1112))
    //0x1161..0x1175
    private val nucleus = arrayListOf(Char(0x1161), Char(0x1162), Char(0x1163),
        Char(0x1164), Char(0x1165), Char(0x1166), Char(0x1167),
        Char(0x1168), Char(0x1169), Char(0x116A), Char(0x116B),
        Char(0x116C), Char(0x116D), Char(0x116E), Char(0x116F),
        Char(0x1170), Char(0x1171), Char(0x1172), Char(0x1173),
        Char(0x1174), Char(0x1175))
    //0x11A8..0x11C2
    private val coda = arrayListOf(null, Char(0x11A8), Char(0x11A9), Char(0x11AA),
        Char(0x11AB), Char(0x11AC), Char(0x11AD), Char(0x11AE),
        Char(0x11AF), Char(0x11B0), Char(0x11B1), Char(0x11B2),
        Char(0x11B3), Char(0x11B4), Char(0x11B5), Char(0x11B6),
        Char(0x11B7), Char(0x11B8), Char(0x11B9), Char(0x11BA),
        Char(0x11BB), Char(0x11BC), Char(0x11BD), Char(0x11BE),
        Char(0x11BF), Char(0x11C0), Char(0x11C1), Char(0x11C2))

    // 초성, 중성, 종성 획수
    private val onsetStrokes = listOf(1, 2, 1, 2, 4, 3, 3, 4, 8, 2, 4, 1, 2, 4, 3, 2, 3, 4, 3)
    private val nucleusStrokes = arrayListOf(2, 3, 3, 4, 2, 3, 3, 4, 2, 4, 5, 3, 3, 2, 4, 5, 3, 3, 1, 2, 1)
    private val codaStrokes = arrayListOf(0, 1, 2, 3, 1, 3, 4, 2, 3, 4, 6, 7, 5, 6, 7, 6, 3, 4, 6, 2, 4, 1, 2, 3, 2, 3, 4, 3)

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

    fun strokes(label: String): Int {
        val dissemble = ArrayList<Char>()
        val s = ArrayList<Int>()
        var sum = 0
        for (i in label.indices) {
            val char = label[i].dissembleHangul()?.toList() as List<Char>
            for (j in char.indices) {
                dissemble.add(char[j])
                when (j) {
                    0 -> s.add(onsetStrokes[onset.indexOf(char[j])])
                    1 -> s.add(nucleusStrokes[nucleus.indexOf(char[j])])
                    2 -> s.add(codaStrokes[coda.indexOf(char[j])])
                }
            }
        }
        for (i in s.indices) {
            sum+=s[i]
        }
        /*
        println(dissemble)
        println(s)
        println(sum)
        */
        return sum
    }

    data class CheckResult(val correct: Boolean, val label: String, val answer: String)
}
