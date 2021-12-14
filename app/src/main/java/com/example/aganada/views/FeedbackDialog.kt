package com.example.aganada.views

import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.aganada.R
import com.example.aganada.databinding.DialogFeedbackBinding

class FeedbackDialog: DialogFragment() {
    var title: String = ""
    var content: String = ""
    private lateinit var rightButtonText: String
    private lateinit var leftButtonText: String
    private var contentRes: Int = 0
    private var mediaRes: Int = 0
    private lateinit var mediaPlayer: MediaPlayer
    private var isVibrate: Boolean = false
    private lateinit var vibrator: Vibrator

    init {
        setCorrect(false)
    }

    private lateinit var binding: DialogFeedbackBinding
    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mediaPlayer = MediaPlayer.create(requireContext(), mediaRes)
    }

    fun setCorrect(correct: Boolean) {
        if (correct) {
            title = "맞았어요"
            leftButtonText = "다른 사진 찍기"
            rightButtonText = "단어장 보기"
            contentRes = R.drawable.ic_good_face
            mediaRes = R.raw.good
            isVibrate = false
        } else {
            title = "틀렸어요"
            leftButtonText = "다시풀기"
            rightButtonText = "단어장 보기"
            contentRes = R.drawable.ic_bad_face
            mediaRes = R.raw.retry
            isVibrate = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            Dialog(it, R.style.DetailDialog)
        }?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFeedbackBinding.inflate(inflater, container, false)
        binding.textViewTitle.text = title
        binding.buttonLeft.text = leftButtonText
        binding.buttonRight.text = rightButtonText
        binding.imageViewContent.setImageResource(contentRes)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonLeft.setOnClickListener(onClickListener)
        binding.buttonRight.setOnClickListener(onClickListener)
        mediaPlayer.start()
    }
}