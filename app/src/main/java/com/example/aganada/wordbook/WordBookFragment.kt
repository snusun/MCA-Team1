package com.example.aganada.wordbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.aganada.R
import com.example.aganada.databinding.FragmentWordBookBinding
import android.view.Gravity
import android.widget.*
import androidx.core.view.setPadding


class WordBookFragment : Fragment() {
    private var _binding: FragmentWordBookBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        binding.addButton.setOnClickListener {
            FlipCard.create(
                binding.gridLayout, R.drawable.ic_launcher_foreground, "Text").attach()
        }

        return view
    }
}
