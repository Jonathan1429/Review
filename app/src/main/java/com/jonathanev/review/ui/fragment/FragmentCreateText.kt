package com.jonathanev.review.ui.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
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
            private var seAgregoSaltoDeLinea = false
            private var startChange = 0
            private var countChange = 0
            private var beforeChange = 0

            // Aquí guardaremos una copia de seguridad de los estilos EN TIEMPO REAL
            private var copiaEstilos = SpannableStringBuilder()

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Antes de que el teclado rompa algo, le tomamos una foto exacta a TODO el texto con sus colores
                if (s is Spannable) {
                    copiaEstilos = SpannableStringBuilder(s)
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                startChange = start
                countChange = count
                beforeChange = before

                seAgregoSaltoDeLinea = count > before &&
                        s?.subSequence(start, start + count)?.contains("\n") == true
            }

            override fun afterTextChanged(texto: Editable?) {
                if (texto == null || seAgregoSaltoDeLinea) return

                val esBorrado = beforeChange >= countChange

                binding.etPregResp.removeTextChangedListener(this)

                try {
                    if (esBorrado) {
                        // Si borró, la copia anterior ya no sirve para adelante.
                        // Actualizamos la copia con lo que sobrevivió al borrado.
                        copiaEstilos = SpannableStringBuilder(texto)
                    } else {
                        // 1️⃣ RESTAURAR LO ANTERIOR:
                        // Traemos los colores guardados en la "foto" (antes del cambio) y los re-estampamos.
                        // Esto recupera instantáneamente lo que Gboard haya borrado de la palabra actual.
                        val finViejo = minOf(copiaEstilos.length, texto.length)
                        val spansViejos = copiaEstilos.getSpans(0, finViejo, ForegroundColorSpan::class.java)

                        for (span in spansViejos) {
                            val start = copiaEstilos.getSpanStart(span)
                            val end = copiaEstilos.getSpanEnd(span)
                            if (start < texto.length && end <= texto.length) {
                                texto.setSpan(
                                    ForegroundColorSpan(span.foregroundColor),
                                    start,
                                    end,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                        }

                        // 2️⃣ PINTAR LO NUEVO:
                        // Ahora que lo viejo está a salvo, pintamos el rango nuevo con el color actual letra por letra
                        if (colorActual != Color.WHITE) {
                            val finDelCambio = startChange + countChange
                            val letrasNuevasReales = countChange - beforeChange
                            val inicioPintadoReal = maxOf(startChange, finDelCambio - letrasNuevasReales)

                            for (i in inicioPintadoReal until finDelCambio) {
                                if (i >= 0 && i < texto.length) {
                                    texto.setSpan(
                                        ForegroundColorSpan(colorActual),
                                        i,
                                        i + 1,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                            }
                        }

                        // 3️⃣ Guardamos el estado actual en la copia para la siguiente pulsación de tecla
                        copiaEstilos = SpannableStringBuilder(texto)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    binding.etPregResp.addTextChangedListener(this)
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

    fun Editable.consolidarSpansDeColor() {
        // 1️⃣ Obtenemos todos los spans de color actuales
        val spans = getSpans(0, length, ForegroundColorSpan::class.java) ?: return
        if (spans.isEmpty()) return

        // 2️⃣ Agrupamos los spans que tienen exactamente el mismo color
        val gruposPorColor = spans.groupBy { it.foregroundColor }

        for ((color, listaSpans) in gruposPorColor) {
            // Encontramos los bloques continuos de este color
            val rangos = listaSpans.map { span ->
                getSpanStart(span) to getSpanEnd(span)
            }.sortedBy { it.first } // Los ordenamos por su posición de inicio

            if (rangos.isEmpty()) continue

            // Removemos TODOS los spans viejos de este color para limpiar la casa
            listaSpans.forEach { removeSpan(it) }

            // 3️⃣ Fusionamos los rangos contiguos (ej. si uno termina en 2 y el otro empieza en 2)
            var inicioActual = rangos.first().first
            var finActual = rangos.first().second

            for (i in 1 until rangos.size) {
                val (s, e) = rangos[i]
                if (s <= finActual) {
                    // Si se tocan o se enciman, extendemos el final del bloque
                    finActual = maxOf(finActual, e)
                } else {
                    // Si hay un hueco, cerramos el bloque anterior, lo pintamos y abrimos uno nuevo
                    setSpan(ForegroundColorSpan(color), inicioActual, finActual, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    inicioActual = s
                    finActual = e
                }
            }
            // Pintamos el último bloque consolidado
            setSpan(ForegroundColorSpan(color), inicioActual, finActual, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    private fun saveCurrentQuestion(): SpanPalabraModel {
        binding.etPregResp.text?.consolidarSpansDeColor()
        val editable = binding.etPregResp.text

        val listSpans = editable?.let { text ->
            val totalLength = text.length
            text.getSpans(0, totalLength, ForegroundColorSpan::class.java).map { span ->
                ColorRangeUi(
                    start = text.getSpanStart(span),
                    end = text.getSpanEnd(span),
                    color = span.foregroundColor
                )
            }
        } ?: emptyList()

        /*val resColocarEtiquetas =
            sharedViewModel.setColocarEtiquetas(editable.toString(), listSpans)*/
        sharedViewModel.addTextContent(editable.toString(), listSpans)

        return SpanPalabraModel()
        /*return if (isDoubleColors) {
            SpanPalabraModel(
                message = "Sobreescribiste colores y mantuvimos los últimos seleccionados",
                isDoubleColors = true
            )
        } else {
            SpanPalabraModel()
        }*/
    }

    private fun Editable.toPaintingLetters(start: Int, end: Int, color: Int) {
        if (start < 0 || end > this.length || start >= end) return

        // Pintamos letra por letra en el rango especificado
        for (i in start until end) {
            // Removemos Spans de color previos estrictamente en esta posición de 1 carácter
            val spansViejos = getSpans(i, i + 1, ForegroundColorSpan::class.java)
            for (span in spansViejos) {
                removeSpan(span)
            }

            // Aplicamos el color de forma quirúrgica a esta única letra
            setSpan(
                ForegroundColorSpan(color),
                i,
                i + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE // Al ser exclusivo en ambos lados, no se estira ni contamina vecinos
            )
        }
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