package com.jonathanev.review.Fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.jonathanev.review.Activities.Activity_Cuestionario
import com.jonathanev.review.databinding.FragmentColoresBinding
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorListener
class Fragment_DialogColores_popup : DialogFragment() {
    private var binding: FragmentColoresBinding? = null
    private var colorActual = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentColoresBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animación cuando se esté seleccionando un color.
        val bubbleFlag = BubbleFlag(context)
        bubbleFlag.flagMode = FlagMode.FADE
        binding!!.colorPickerView.flagView = bubbleFlag
        binding!!.colorPickerView.setColorListener(ColorListener { color, fromUser ->
            setLayoutColor(color)
            colorActual = color
            val actividCuestionario = activity as Activity_Cuestionario?
            actividCuestionario!!.colorActual(colorActual)
        })
        val fragment: Fragment = this
        binding!!.btnContinuar.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        binding!!.btnDefault.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().remove(fragment).commit()
            val intent = Intent(activity, Activity_Cuestionario::class.java)
            colorActual = Color.BLACK
            val actividCuestionario = activity as Activity_Cuestionario?
            actividCuestionario!!.colorActual(colorActual)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setLayoutColor(color: Int) {
        binding!!.colorHexadecimal.text = "#" + binding!!.colorPickerView.colorEnvelope.hexCode
        if (binding!!.colorHexadecimal.text.toString() == "#FF000000") {
            binding!!.btnContinuar.setTextColor(Color.WHITE)
        } else {
            binding!!.btnContinuar.setTextColor(Color.BLACK)
        }
        binding!!.btnContinuar.setBackgroundColor(color)
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