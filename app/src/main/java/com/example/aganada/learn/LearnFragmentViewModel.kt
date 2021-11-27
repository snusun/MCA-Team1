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
import kr.bydelta.koala.dissembleHangul
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.reflect.typeOf

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
    private val onset = arrayListOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
        'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    private val nucleus = arrayListOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
    private val coda = arrayListOf(Character.MIN_VALUE, 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ',
        'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ',
        'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')

    // 초성, 중성, 종성 획수
    private val onsetStrokes = arrayListOf(1, 2, 1, 2, 4, 3, 3, 4, 8, 2, 4, 1, 2, 4, 3, 2, 3, 4, 3)
    private val nucleusStrokes = arrayListOf(2, 3, 3, 4, 2, 3, 3, 4, 2, 4, 5, 3, 3, 2, 4, 5, 3, 3, 1, 2, 1)
    private val codaStrokes = arrayListOf(0, 1, 2, 3, 1, 3, 4, 2, 3, 4, 6, 7, 5, 6, 7, 6, 3, 4, 6, 2, 4, 1, 2, 3, 2, 3, 4, 3)

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

    fun strokes(label: String): Int {
        val dissemble = ArrayList<Char>()
        val s = ArrayList<Int>()
        var sum = 0
        for (i in label.indices) {
            val char = label[i].dissembleHangul()?.toList() as List<Char>
            println(char)
            for (j in char.indices) {
                dissemble.add(char[j])
                println("indices")
                println(char[j])
                if (j==0) {
                    println(onset.indexOf(char[j]))
                    println('ㄱ')
                    println(onset.indexOf('ㄱ'))
                    println(onsetStrokes[0])
                    onsetStrokes[onset.indexOf(char[j])]
                }
                /*
                when (j) {
                    0 -> s.add(onsetStrokes[onset.indexOf(char[j])])
                    1 -> s.add(nucleusStrokes[nucleus.indexOf(char[j])])
                    2 -> s.add(codaStrokes[coda.indexOf(char[j])])
                }*/
            }
        }
        for (i in s.indices) {
            sum+=s[i]
        }
        println(dissemble)
        println(onset)
        println(s)
        println(sum)
        return sum
    }

    data class CheckResult(val correct: Boolean, val label: String, val answer: String)
}
