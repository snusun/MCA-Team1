package com.example.aganada.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.aganada.R
import com.example.aganada.camera.CameraXActivity
import com.example.aganada.databinding.FragmentTestBinding
import com.example.aganada.views.WordView

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        val view = binding.root

        setView()
        setObserve()

        binding.wordView.word = "워드"
        binding.cameraButton.setOnClickListener {
            val intent = Intent(context, CameraXActivity::class.java)
            startActivity(intent)
        }
        binding.wordbookButton.setOnClickListener {
            view.findNavController().navigate(
                R.id.action_testFragment_to_wordBookFragment)
        }

        return view
    }

    private fun setView() {
        binding.apply {
            undoButton.setOnClickListener { wordView.unDo() }
            redoButton.setOnClickListener { wordView.reDo() }
            checkButton.setOnClickListener {
                Log.v("LearnFragment", "Check button clicked.")
            }
        }
    }

    private fun setObserve() {
        viewModel.apply {
            drawMode.observe(viewLifecycleOwner) {
                if (it == WordView.DrawMode.PENCIL) {
                    binding.drawModeButton.setImageResource(R.drawable.ic_baseline_eraser_24)
                    binding.wordView.drawMode = WordView.DrawMode.PENCIL
                } else if (it == WordView.DrawMode.ERASER) {
                    binding.drawModeButton.setImageResource(R.drawable.ic_baseline_pencil_24)
                    binding.wordView.drawMode = WordView.DrawMode.ERASER
                }
            }

            photo.observe(viewLifecycleOwner) {
                Glide.with(this@TestFragment)
                    .load(it)
                    .into(binding.wordImage)
                binding.wordImage
            }
        }
    }

    override fun onResume() {
        viewModel.loadPhoto(requireContext())
        super.onResume()
    }

}