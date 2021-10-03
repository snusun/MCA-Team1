package com.example.aganada.learn

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

class LearnFragment : Fragment() {
    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.wordbookButton.setOnClickListener {
            binding.root.findNavController().navigate(
                R.id.action_learnFragment_to_wordBookFragment)
        }

        binding.cameraButton.setOnClickListener {
            binding.root.findNavController().navigate(
                R.id.action_learnFragment_to_cameraFragment2)
        }

        return view
    }
}