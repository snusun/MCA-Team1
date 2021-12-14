package com.example.aganada.test

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.aganada.R
import com.example.aganada.camera.CameraXActivity
import com.example.aganada.databinding.FragmentTestBinding
import com.example.aganada.learn.LearnFragmentViewModel
import com.example.aganada.views.FinishDialog
import com.example.aganada.views.WordView
import kotlinx.android.synthetic.main.fragment_test.*

class TestFragment : Fragment() {
    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TestFragmentViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return TestFragmentViewModel() as T
            }
        }).get(TestFragmentViewModel::class.java)
    }
    private val dialog = FinishDialog()

    private lateinit var goodPlayer: MediaPlayer
    private lateinit var retryPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        val view = binding.root

        setView()
        setObserve()

        goodPlayer = MediaPlayer.create(requireContext(), R.raw.good)
        retryPlayer = MediaPlayer.create(requireContext(), R.raw.retry)

        return view
    }

    private fun setView() {
        binding.apply {
            undoButton.setOnClickListener { wordView.unDo() }
            redoButton.setOnClickListener { wordView.reDo() }
            checkButton.setOnClickListener {
                this@TestFragment.viewModel.recognizeText(wordView.pathSet)
            }
            nextButton.setOnClickListener { this@TestFragment.viewModel.getNext() }
            prevButton.setOnClickListener { this@TestFragment.viewModel.getPrev() }
            shuffleButton.setOnClickListener { this@TestFragment.viewModel.onShuffleClicked() }

            cameraButton.setOnClickListener {
                val intent = Intent(context, CameraXActivity::class.java)
                startActivity(intent)
            }
            wordbookButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_testFragment_to_wordBookFragment)
            }
        }

    }

    private fun setObserve() {
        viewModel.apply {
            drawMode.observe(viewLifecycleOwner) {
                this@TestFragment.onDrawModeChange(it)
            }

            label.observe(viewLifecycleOwner) {
                binding.wordView.word = it
                wordView.clear()
            }

            photo.observe(viewLifecycleOwner) {
                Glide.with(this@TestFragment)
                    .load(it)
                    .into(binding.wordImage)
                binding.wordImage
            }

            checkResult.observe(viewLifecycleOwner) {
                onCheckResultOut(it)
            }

            index.observe(viewLifecycleOwner) {
                binding.prevButton.imageTintList = (
                    if (it <= 0) ColorStateList.valueOf(Color.LTGRAY)
                    else ColorStateList.valueOf(resources.getColor(R.color.bg_700, null)))
                binding.nextButton.imageTintList = (
                    if (it >= wordbook.value!!.size - 1) ColorStateList.valueOf(Color.LTGRAY)
                    else ColorStateList.valueOf(resources.getColor(R.color.bg_700, null)))
            }

            shuffled.observe(viewLifecycleOwner) {
                val image = if (it) R.drawable.ic_baseline_trending_flat_24
                            else R.drawable.ic_baseline_shuffle_24
                binding.shuffleButton.setImageResource(image)
            }
        }
    }

    private fun onDrawModeChange(drawMode: WordView.DrawMode) {
        if (drawMode == WordView.DrawMode.PENCIL) {
            binding.drawModeButton.setImageResource(R.drawable.ic_baseline_eraser_24)
            binding.wordView.drawMode = WordView.DrawMode.PENCIL
        } else if (drawMode == WordView.DrawMode.ERASER) {
            binding.drawModeButton.setImageResource(R.drawable.ic_baseline_pencil_24)
            binding.wordView.drawMode = WordView.DrawMode.ERASER
        }
    }

    private fun onCheckResultOut(checkResult: LearnFragmentViewModel.CheckResult) {
        if (checkResult.working){
            return
        }
        if (checkResult.correct) {
            if ((viewModel.index.value?: 0) + 1 == viewModel.wordbook.value?.size) {
                showFinishModal()
            }
            Toast.makeText(context, "참 잘했어요!!", Toast.LENGTH_SHORT).show()
            goodPlayer.start()
            viewModel.getNext()
        } else {
            Toast.makeText(context, "다시한번 써볼까요?", Toast.LENGTH_SHORT).show()
            retryPlayer.start()
            wordView.clear()
        }
    }

    private fun showFinishModal() {
        dialog.setOnClickListener {
            when (it.id) {
                R.id.button_confirm -> {
                    this.findNavController().navigate(R.id.action_testFragment_to_wordBookFragment)
                }
                R.id.button_terminate  -> {
                    viewModel.loadPhoto(requireContext())
                }
            }
            dialog.dismiss()
        }
        dialog.title = "참 잘했어"
        dialog.content = "단어장으로 돌아갈까요?"
        dialog.show(parentFragmentManager, "finish dialog")
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPhoto(requireContext())
    }

}