package com.example.aganada.learn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.aganada.PhotoFiles
import com.example.aganada.R
import com.example.aganada.camera.CameraXActivity
import com.example.aganada.views.WordView.DrawMode
import com.example.aganada.databinding.FragmentLearnBinding
import com.example.aganada.views.EditDialog
import kotlinx.android.synthetic.main.fragment_learn.view.*
import kotlinx.android.synthetic.main.fragment_test.*
import java.io.File

class LearnFragment : Fragment() {
    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LearnFragmentViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LearnFragmentViewModel() as T
            }
        }).get(LearnFragmentViewModel::class.java)
    }
    private val dialog = EditDialog()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("LearnFragment", "onCreateView")
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_learn, container, false)
        binding.viewModel = viewModel

        setView()
        setObserve()

        return binding.root
    }

    private fun setView() {
        binding.apply {
            undoButton.setOnClickListener { wordView.unDo() }
            redoButton.setOnClickListener { wordView.reDo() }
            checkButton.setOnClickListener {
                Log.v("LearnFragment", "Check button clicked.")
                this@LearnFragment.viewModel.recognizeText(wordView.pathSet)
            }

            cameraButton.setOnClickListener {
                val intent = Intent(context, CameraXActivity::class.java)
                startActivity(intent)
            }

            wordbookButton.setOnClickListener {
                findNavController().navigate(R.id.action_learnFragment_to_wordBookFragment)
            }

            editButton.setOnClickListener {
                this@LearnFragment.showEditDialog(this@LearnFragment.viewModel.photo.value!!)
            }
        }
    }

    private fun setObserve() {
        viewModel.apply {
            drawMode.observe(viewLifecycleOwner) {
                this@LearnFragment.onDrawModeChange(it)
            }

            label.observe(viewLifecycleOwner) {
                binding.wordView.word = it
            }

            photo.observe(viewLifecycleOwner) {
                Glide.with(this@LearnFragment)
                    .load(it)
                    .into(binding.wordImage)
                binding.wordImage
            }

            checkResult.observe(viewLifecycleOwner) {
                onCheckResultOut(it)
            }
        }
    }

    private fun onDrawModeChange(drawMode: DrawMode) {
        if (drawMode == DrawMode.PENCIL) {
            binding.drawModeButton.setImageResource(R.drawable.ic_baseline_eraser_24)
            binding.wordView.drawMode = DrawMode.PENCIL
        } else if (drawMode == DrawMode.ERASER) {
            binding.drawModeButton.setImageResource(R.drawable.ic_baseline_pencil_24)
            binding.wordView.drawMode = DrawMode.ERASER
        }
    }

    private fun onCheckResultOut(checkResult: LearnFragmentViewModel.CheckResult) {
        if (checkResult.correct) {
            Toast.makeText(context, "참 잘했어요!!", Toast.LENGTH_SHORT).show()
            viewModel.movePhotoToWordBook()
            findNavController().navigate(R.id.action_learnFragment_to_wordBookFragment)
        } else {
            Toast.makeText(context, "다시한번 써볼까요?", Toast.LENGTH_SHORT).show()
            wordView.clear()
        }
    }

    private fun showEditDialog(photo: File) {
        val originLabel = PhotoFiles.getLabel(photo.absolutePath)
        dialog.originLabel = originLabel
        dialog.setOnClickListener {
            when (it.id) {
                R.id.button_terminate -> {
                    dialog.dismiss()
                }
                R.id.button_confirm -> {
                    dialog.dismiss()
                    var newLabel = dialog.binding.editTextContent.text.toString().trim()
                    if (newLabel.isBlank()) {
                        newLabel = originLabel
                    }
                    val newFile = File(photo.parent, photo.name.replace(originLabel, newLabel))
                    photo.renameTo(newFile)
                    activity?.intent?.extras?.putString("captured_image_name", newFile.absolutePath)
                    viewModel.loadPhoto(newFile.absolutePath)
                }
            }
        }
        dialog.show(parentFragmentManager, "edit dialog")
    }

    override fun onResume() {
        super.onResume()
        val filename = activity?.intent?.getStringExtra("captured_image_name")?: return
        viewModel.loadPhoto(filename)
    }

}
