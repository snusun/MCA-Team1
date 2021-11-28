package com.example.aganada.views

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.aganada.R
import com.example.aganada.databinding.DialogEditBinding

class EditDialog: DialogFragment() {
    var originLabel: String = ""

    lateinit var binding: DialogEditBinding
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
        binding = DialogEditBinding.inflate(inflater, container, false)
        binding.editTextContent.hint = originLabel
        binding.editTextContent.setText(originLabel)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonConfirm.setOnClickListener(onClickListener)
        binding.buttonTerminate.setOnClickListener(onClickListener)
    }
}