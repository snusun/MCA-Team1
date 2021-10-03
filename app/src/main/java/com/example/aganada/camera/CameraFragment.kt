package com.example.aganada.camera

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

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.learnButton.setOnClickListener {
            binding.root.findNavController().navigate(
                R.id.action_cameraFragment_to_learnFragment2)
        }

        binding.wordbookButton.setOnClickListener {
            binding.root.findNavController().navigate(
                R.id.action_cameraFragment_to_wordBookFragment)
        }

        return view
    }
}