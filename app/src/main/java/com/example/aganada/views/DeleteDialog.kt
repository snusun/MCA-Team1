package com.example.aganada.views

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.aganada.R
import com.example.aganada.databinding.DialogDeleteBinding

class DeleteDialog: DialogFragment() {
    var title: String = ""
    val content: String = "이 카드를 지울까요?"

    private lateinit var binding: DialogDeleteBinding
    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            Dialog(it, R.style.DetailDialog)
        }?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogDeleteBinding.inflate(inflater, container, false)
        binding.textViewTitle.post {
            binding.textViewTitle.text = title
        }
        binding.textViewContent.post {
            binding.textViewContent.text = content
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDelete.setOnClickListener(onClickListener)
        binding.buttonCancel.setOnClickListener(onClickListener)
    }
}