package com.jonathanev.review.UI.View.Fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.jonathanev.review.UI.View.ActivityModificar
import com.jonathanev.review.UI.ViewModel.Fragments.Fragment_DialogColores_popupViewModel
import com.jonathanev.review.databinding.FragmentColoresBinding
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentDialogColoresPopup : DialogFragment() {
    private lateinit var binding: FragmentColoresBinding
    private val viewModel: Fragment_DialogColores_popupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentColoresBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animación cuando se esté seleccionando un color.
        val bubbleFlag = BubbleFlag(context)
        bubbleFlag.flagMode = FlagMode.FADE
        binding.colorPickerView.flagView = bubbleFlag

        // Observamos cambios de color desde el ViewModel
        viewModel.colorSeleccionado.observe(viewLifecycleOwner) { color ->
            setLayoutColor(color)
            (activity as? ActivityModificar)?.setColor(color)
        }

        binding.colorPickerView.setColorListener(ColorListener { color, _ ->
            viewModel.setColor(color)
        })

        binding.btnContinuar.setOnClickListener {
            dismiss()
        }

        binding.btnDefault.setOnClickListener {
            viewModel.resetColor()
            dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setLayoutColor(color: Int) {
        binding.colorHexadecimal.text = "#" + binding.colorPickerView.colorEnvelope.hexCode
        if (binding.colorHexadecimal.text.toString() == "#FF000000") {
            binding.btnContinuar.setTextColor(Color.WHITE)
        } else {
            binding.btnContinuar.setTextColor(Color.BLACK)
        }
        binding.btnContinuar.setBackgroundColor(color)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(700, 1000)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}