package com.jonathanev.review.UI.View.Fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.Core.Constants.BASERUTA_IMG_CIFRADO
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.FragmentCreateTextViewModel
import com.jonathanev.review.databinding.FragmentCreateFilesBinding
import com.jonathanev.review.databinding.FragmentCreateTextBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentCreateText : Fragment() {
    private var _binding: FragmentCreateTextBinding? = null
    private val binding get() = _binding!!

    private var colorActual: Int = 0
    private var longCaracteres = 0

    private val viewModel: FragmentCreateTextViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initListeners()
    }

    private fun initUI() {
        setColor(Color.WHITE)
    }

    private fun initListeners() {
        binding.imgvSelColor.setOnClickListener {
            val dialog = FragmentDialogColoresPopup()
            dialog.show(parentFragmentManager, "FragmentColor")
        }

        // Eliminar textos con colores
        binding.imgvQuitColor.setOnClickListener {
            val text = binding.etPregResp.text
            val spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(text)
            spannableStringBuilder.clearSpans()

            binding.etPregResp.text = spannableStringBuilder

            colorActual = Color.WHITE
            setColor(colorActual)
        }

        binding.etPregResp.addTextChangedListener(object : TextWatcher {
            private var textoAnterior: String = ""
            private var seAgregoSaltoDeLinea = false

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Guarda el texto antes del cambio
                textoAnterior = s?.toString() ?: ""
                longCaracteres = binding.etPregResp.length()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Aquí puedes verificar si se ha añadido un salto de línea en este momento
                seAgregoSaltoDeLinea =
                    count > before && s?.subSequence(start, start + count)
                        ?.contains("\n") == true
            }

            override fun afterTextChanged(texto: Editable?) {
                if (!texto.toString()
                        .contains(BASERUTA_IMG_CIFRADO) && (binding.etPregResp.length() - longCaracteres) == 1
                ) {
                    // Si hay un salto de linea o es color negro no se pinta nada
                    if (colorActual != -16777216 && !seAgregoSaltoDeLinea) {
                        val cursorPosition = binding.etPregResp.selectionStart
                        viewModel.setPintarLetra(
                            texto!!,
                            cursorPosition,
                            colorActual
                        )
                    }
                }
            }
        })

        parentFragmentManager.setFragmentResultListener("colorKey", this) { _, bundle ->
            val color = bundle.getInt("color")
            setColor(color)
        }

        binding.
    }

    fun setColor(@ColorInt color: Int?) {
        if (color == null) {
            ImageViewCompat.setImageTintList(binding.imgvSelColor, null)
            return
        }
        ImageViewCompat.setImageTintMode(binding.imgvSelColor, PorterDuff.Mode.SRC_ATOP)
        ImageViewCompat.setImageTintList(binding.imgvSelColor, ColorStateList.valueOf(color))
        colorActual = color
    }
}