package com.jonathanev.review.ui.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.databinding.FragmentCreateTextBinding
import com.jonathanev.review.presentation.model.ColorRangeUi
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.model.SpanPalabraModel
import com.jonathanev.review.presentation.viewmodel.MainToolbarViewModel
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

class FragmentCreateText : Fragment() {
    private var _binding: FragmentCreateTextBinding? = null
    private val binding get() = _binding!!
    private val viewModelToolbar: MainToolbarViewModel by activityViewModels()
    private val sharedViewModel: SharedFragmentCreateFileViewModel by activityViewModels()
    private var colorActual: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arguments?.getParcelable(
            "questionText", QuestionContentUi.Text::class.java
        ) ?: QuestionContentUi.None

        initUI(data)
        initListeners()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModelToolbar.onSave.collect {
                        saveProcess()
                    }
                }

                launch {
                    viewModelToolbar.onBefore.collect {
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    private fun saveProcess() {
        if (binding.etPregResp.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "Debes asignar un texto", Toast.LENGTH_LONG).show()
            return
        }

        val response = saveCurrentQuestion()
        if (response.isDoubleColors) {
            Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
        }
        findNavController().navigateUp()
    }

    @SuppressLint("NewApi")
    private fun initUI(data: QuestionContentUi) {
        when (data) {
            is QuestionContentUi.Image -> Unit
            QuestionContentUi.None -> Unit
            is QuestionContentUi.Text -> {
                val builder = data.toSpannable(data.text, data.colorRanges)
                binding.etPregResp.text = builder
            }
        }

        setColor(Color.WHITE)
        viewModelToolbar.changeTitle("")
        viewModelToolbar.isBtnBackVisible(true)
        viewModelToolbar.isBtnSaveVisible(true)
    }

    private fun initListeners() {
        binding.imgvSelColor.setOnClickListener {
            val dialog = FragmentDialogColoresPopup()
            dialog.show(parentFragmentManager, "FragmentColor")
        }

        // Eliminar textos con colores
        binding.imgvQuitColor.setOnClickListener {
            val text = binding.etPregResp.text
            val spannableStringBuilder = SpannableStringBuilder(text)
            spannableStringBuilder.clearSpans()

            binding.etPregResp.text = spannableStringBuilder

            colorActual = Color.WHITE
            setColor(colorActual)
        }

        binding.etPregResp.addTextChangedListener(object : TextWatcher {
            private var textoAnterior: String = ""
            private var seAgregoSaltoDeLinea = false
            private var caracteresAntesDelCambio = 0

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
                // Guarda el texto antes del cambio
                textoAnterior = s?.toString() ?: ""
                caracteresAntesDelCambio = binding.etPregResp.text.toString().length
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Aquí puedes verificar si se ha añadido un salto de línea en este momento
                seAgregoSaltoDeLinea =
                    count > before && s?.subSequence(start, start + count)?.contains("\n") == true
            }

            override fun afterTextChanged(texto: Editable?) {
                if ((binding.etPregResp.text.toString().length - caracteresAntesDelCambio) == 1
                    && !seAgregoSaltoDeLinea
                    && (colorActual != Color.WHITE)) {
                    // Si hay un salto de linea o es color negro no se pinta nada
                    val cursorPosition = binding.etPregResp.selectionStart

                    binding.etPregResp.post {
                        texto!!.toPaintingLetters(cursorPosition, colorActual)
                    }
                    /*viewModel.setPintarLetra(
                        texto,
                        cursorPosition,
                        colorActual
                    )*/
                }
            }
        })

        parentFragmentManager.setFragmentResultListener("colorKey", this) { _, bundle ->
            val color = bundle.getInt("color")
            setColor(color)
        }
    }

    private fun setColor(@ColorInt color: Int?) {
        if (color == null) {
            ImageViewCompat.setImageTintList(binding.imgvSelColor, null)
            return
        }
        ImageViewCompat.setImageTintMode(binding.imgvSelColor, PorterDuff.Mode.SRC_ATOP)
        ImageViewCompat.setImageTintList(binding.imgvSelColor, ColorStateList.valueOf(color))
        colorActual = color
    }

    private fun saveCurrentQuestion(): SpanPalabraModel {
        val editable = binding.etPregResp.text

        val colorSpans: Array<ForegroundColorSpan> =
            editable!!.getSpans(0, editable.length, ForegroundColorSpan::class.java)
        val oldEditable = editable
        val sortedSpans = colorSpans.sortedBy { editable.getSpanStart(it) }

        var start = -1
        var end = 0
        var endAnterior = 0
        var isColNuevo = false
        var colorAnterior = 0
        var colorNuevo = 0
        var isDoubleColors = false
        var toCleaningColors = false

        for (colorSpan: ForegroundColorSpan in sortedSpans) {
            if (start == -1) {
                start = editable.getSpanStart(colorSpan)
            }

            if (end > editable.getSpanStart(colorSpan)) {
                isDoubleColors = true
                toCleaningColors = true
            }

            end = editable.getSpanEnd(colorSpan)
            colorNuevo = colorSpan.foregroundColor

            if (colorAnterior != colorNuevo) {
                if (colorAnterior == 0) {
                    isColNuevo = false
                    colorAnterior = colorNuevo
                    endAnterior = end - 1
                } else {
                    isColNuevo = true
                }
            }

            //if ((end - endAnterior) != 1) {
            // Limpiar colores encimados
            if (toCleaningColors) {
                // Obtener los spans dentro del rango especificado
                val spansToRemove = oldEditable.getSpans(
                    start, endAnterior, ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    if (span.foregroundColor == colorAnterior) {
                        editable.removeSpan(span)
                    }
                }

                start = editable.getSpanStart(colorSpan)
                endAnterior = end

                colorAnterior = colorNuevo
                isColNuevo = false
                toCleaningColors = false
            } else if (isColNuevo || (end - endAnterior) != 1) {
                // Obtener los spans dentro del rango especificado
                val spansToRemove = oldEditable.getSpans(
                    start, endAnterior, ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    if (span.foregroundColor == colorAnterior) {
                        editable.removeSpan(span)
                    }
                }

                editable.setSpan(
                    ForegroundColorSpan(colorAnterior),
                    start,
                    endAnterior,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                start = editable.getSpanStart(colorSpan)
                endAnterior = end

                colorAnterior = colorNuevo
                isColNuevo = false
                // toCleaningColors = false
            } else {
                endAnterior = end
            }
        }

        // Remove old spans
        if (colorSpans.isNotEmpty()) {
            val spansToRemove =
                oldEditable.getSpans(start, endAnterior, ForegroundColorSpan::class.java)

            for (span in spansToRemove) {
                if (span.foregroundColor == colorAnterior) {
                    editable.removeSpan(span)
                }
            }

            editable.setSpan(
                ForegroundColorSpan(colorAnterior),
                start,
                endAnterior,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val listSpans = binding.etPregResp.text!!.getSpans(
            0, editable.length, ForegroundColorSpan::class.java
        ).map { span ->
            ColorRangeUi(
                start = editable.getSpanStart(span),
                end = editable.getSpanEnd(span),
                color = span.foregroundColor
            )
        }

        /*val resColocarEtiquetas =
            sharedViewModel.setColocarEtiquetas(editable.toString(), listSpans)*/
        sharedViewModel.addTextContent(editable.toString(), listSpans)

        return if (isDoubleColors) {
            SpanPalabraModel(
                message = "Sobreescribiste colores y mantuvimos los últimos seleccionados",
                isDoubleColors = true
            )
        } else {
            SpanPalabraModel()
        }
    }

    private fun Editable.toPaintingLetters(positionCursor: Int, color: Int){
        if (positionCursor <= 0) return
        val start = positionCursor - 1

        // 1️⃣ Quitar spans de color previos en esa posición
        getSpans(start, positionCursor, ForegroundColorSpan::class.java)
            .forEach { removeSpan(it) }

        setSpan(
            ForegroundColorSpan(color),
            positionCursor - 1,    // Painting color - start
            positionCursor,        // Painting color - end
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun QuestionContentUi.Text.toSpannable(
        text: String, colorRangeDomains: List<ColorRangeUi>
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)

        for (colorRange in colorRangeDomains) {
            val colorSpan = ForegroundColorSpan(colorRange.color)
            builder.setSpan(
                colorSpan, colorRange.start, colorRange.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return builder
    }
}