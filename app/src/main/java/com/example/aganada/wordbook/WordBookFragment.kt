package com.example.aganada.wordbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.aganada.R
import com.example.aganada.databinding.FragmentCameraBinding
import com.example.aganada.databinding.FragmentLearnBinding
import com.example.aganada.databinding.FragmentWordBookBinding

class WordBookFragment : Fragment() {
    private var _binding: FragmentWordBookBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWordBookBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.cameraButton.setOnClickListener {
            binding.root.findNavController().navigate(
                R.id.action_wordBookFragment_to_cameraFragment)
        }

        binding.testButton.setOnClickListener {
            binding.root.findNavController().navigate(
                R.id.action_wordBookFragment_to_testFragment)
        }

        return view
    }
}