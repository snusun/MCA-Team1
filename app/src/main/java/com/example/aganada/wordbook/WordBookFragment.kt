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
            addImage(binding.gridLayout, R.drawable.ic_launcher_foreground, "Text")
        }

        return view
    }

    private fun addImage(layout: GridLayout, imageId: Int, text: String) {
        val imageView = ImageView(this.activity)
        val textView = TextView(this.activity)

        val width = layout.width / 4
        val height = width * 3 / 4
        val newLayout = FrameLayout(requireContext())
        newLayout.setPadding(5)
        newLayout.layoutParams = LinearLayout.LayoutParams(width, height)

        imageView.setImageResource(imageId)
        textView.text = text
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.gravity = Gravity.CENTER
        imageView.setBackgroundResource(R.drawable.background_card)
        textView.setBackgroundResource(R.drawable.background_card)

        imageView.setOnClickListener {
            textView.animate().rotationY(270f).withEndAction {
                textView.visibility= View.VISIBLE
                textView.animate().rotationY(360f)
            }
            imageView.animate().rotationY(90f).withEndAction {
                imageView.visibility = View.INVISIBLE
                imageView.animate().rotationY(180f)
            }
        }
        textView.setOnClickListener {
            imageView.animate().rotationY(90f).withEndAction {
                imageView.visibility = View.VISIBLE
                imageView.animate().rotationY(0f)
            }
            textView.animate().rotationY(270f).withEndAction {
                textView.visibility = View.INVISIBLE
                textView.animate().rotationY(180f)
            }
        }

        newLayout.addView(textView)
        newLayout.addView(imageView)

        imageView.rotationY = 0f
        imageView.visibility = View.VISIBLE
        textView.rotationY = 180f
        textView.visibility = View.INVISIBLE

        layout.addView(newLayout)
    }
}
