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
import androidx.navigation.fragment.findNavController
import com.example.aganada.R
import com.example.aganada.databinding.FragmentWordBookBinding
import com.example.aganada.camera.CameraXActivity
import com.example.aganada.views.EditDialog
import com.example.aganada.views.DeleteDialog


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

    private val deleteDialog = DeleteDialog()
    private val editDialog = EditDialog()

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
                val layout = FlipCardLayout.create(requireContext(), file).also { card ->
                    card.setTextLongClickListener {
                        showEditDialog(card)
                    }
                    card.setImageLongClickListener {
                        showDeleteDialog(card)
                    }
                }
                layout.load()

                binding.gridLayout.post {
                    layout.resize(binding.gridLayout.measuredWidth, binding.gridLayout.columnCount)
                    binding.gridLayout.addView(layout)
                }
            }
        }

        return view
    }

    private fun showDeleteDialog(card: FlipCardLayout): Boolean {
        deleteDialog.title = card.getLabel()
        deleteDialog.setOnClickListener {
            when (it.id) {
                R.id.button_cancel -> deleteDialog.dismiss()
                R.id.button_delete -> {
                    card.delete()
                    deleteDialog.dismiss()
                }
            }
        }
        deleteDialog.show(parentFragmentManager, "deleteCard dialog")
        return true
    }

    private fun showEditDialog(card: FlipCardLayout): Boolean {
        editDialog.originLabel = card.getLabel()
        editDialog.setOnClickListener {
            when (it.id) {
                R.id.button_terminate -> {
                    editDialog.dismiss()
                }
                R.id.button_confirm -> {
                    val newLabel = editDialog.binding.editTextContent.text.toString().trim()
                    if (newLabel.isBlank()) return@setOnClickListener

                    card.rename(newLabel)
                    editDialog.dismiss()
                }
            }
        }
        editDialog.show(parentFragmentManager, "wordbook edit dialog")
        return true
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadImages(requireContext())
    }
}
