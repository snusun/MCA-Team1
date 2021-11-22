package com.example.aganada.wordbook

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.aganada.R
import com.example.aganada.databinding.FragmentWordBookBinding
import com.example.aganada.camera.CameraXActivity


class WordBookFragment : Fragment() {
    private var _binding: FragmentWordBookBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WordBookFragmentViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WordBookFragmentViewModel() as T
            }
        }).get(WordBookFragmentViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordBookBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.imageButtonCamera.setOnClickListener {
            val intent = Intent(context, CameraXActivity::class.java)
            startActivity(intent)
        }

        binding.imageButtonAndroid.setOnClickListener {
            binding.root.findNavController().navigate(
                R.id.action_wordBookFragment_to_testFragment)
        }

        viewModel.wordbook.observe(viewLifecycleOwner) { fileList ->
            fileList.forEach { file ->
                viewModel.addImage(binding.gridLayout, file)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadImages(requireContext())
    }
}
