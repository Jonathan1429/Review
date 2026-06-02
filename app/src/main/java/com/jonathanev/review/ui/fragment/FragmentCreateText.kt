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
    private var segments = mutableListOf<ColorRangeUi>()

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
                segments = data.colorRanges.toMutableList()
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
            private var startChange = 0
            private var beforeChange = 0
            private var countChange = 0

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

                startChange = start
                beforeChange = count
                countChange = after
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) = Unit

            override fun afterTextChanged(editable: Editable?) {

                editable ?: return

                val delta = countChange - beforeChange

                // =========================
                // INSERTAR
                // =========================

                if (delta > 0) {

                    val realStart =
                        binding.etPregResp.selectionStart - delta

                    insertColoredText(
                        start = realStart,
                        length = delta,
                        color = colorActual
                    )
                }

                // =========================
                // BORRAR
                // =========================

                else if (delta < 0) {

                    removeTextRange(
                        start = startChange,
                        length = -delta
                    )
                }

                renderSegments(editable)
            }
        })

        parentFragmentManager.setFragmentResultListener("colorKey", this) { _, bundle ->
            val color = bundle.getInt("color")
            setColor(color)
        }
    }

    private fun removeTextRange(
        start: Int,
        length: Int
    ) {

        val end = start + length

        val newSegments = mutableListOf<ColorRangeUi>()

        segments.forEach { segment ->

            // =========================
            // COMPLETAMENTE ANTES
            // =========================

            if (segment.end <= start) {

                newSegments.add(segment)
            }

            // =========================
            // COMPLETAMENTE DESPUÉS
            // =========================

            else if (segment.start >= end) {

                newSegments.add(
                    segment.copy(
                        start = segment.start - length,
                        end = segment.end - length
                    )
                )
            }

            // =========================
            // INTERSECCIÓN
            // =========================

            else {

                // Parte izquierda
                if (segment.start < start) {

                    newSegments.add(
                        segment.copy(
                            end = start
                        )
                    )
                }

                // Parte derecha
                if (segment.end > end) {

                    newSegments.add(
                        segment.copy(
                            start = start,
                            end = segment.end - length
                        )
                    )
                }
            }
        }

        segments = newSegments
            .filter { it.start < it.end }
            .sortedBy { it.start }
            .toMutableList()

        mergeSegments()
    }

    private fun insertColoredText(
        start: Int,
        length: Int,
        color: Int
    ) {

        val end = start + length

        val newSegments = mutableListOf<ColorRangeUi>()

        segments.forEach { segment ->

            // =========================
            // COMPLETAMENTE ANTES
            // =========================

            if (segment.end <= start) {

                newSegments.add(segment)
            }

            // =========================
            // COMPLETAMENTE DESPUÉS
            // =========================

            else if (segment.start >= start) {

                newSegments.add(
                    segment.copy(
                        start = segment.start + length,
                        end = segment.end + length
                    )
                )
            }

            // =========================
            // INTERSECCIÓN
            // =========================

            else {

                // Parte izquierda
                if (segment.start < start) {

                    newSegments.add(
                        segment.copy(
                            end = start
                        )
                    )
                }

                // Parte derecha
                if (segment.end > start) {

                    newSegments.add(
                        segment.copy(
                            start = end,
                            end = segment.end + length
                        )
                    )
                }
            }
        }

        // =========================
        // NUEVO SEGMENTO
        // =========================

        val expandable = newSegments.indexOfLast {

            it.color == color &&
                    it.end == start
        }

        if (expandable != -1) {

            val old = newSegments[expandable]

            newSegments[expandable] = old.copy(
                end = end
            )

        } else {

            newSegments.add(
                ColorRangeUi(
                    start = start,
                    end = end,
                    color = color
                )
            )
        }

        segments = newSegments

        normalizeSegments()

        mergeSegments()
    }

    private fun normalizeSegments() {

        segments = segments
            .filter { it.start < it.end }
            .distinctBy {
                Triple(it.start, it.end, it.color)
            }
            .sortedBy { it.start }
            .toMutableList()
    }

    private fun renderSegments(editable: Editable) {

        val oldSpans = editable.getSpans(
            0,
            editable.length,
            ForegroundColorSpan::class.java
        )

        oldSpans.forEach {
            editable.removeSpan(it)
        }

        segments.forEach { segment ->

            if (
                segment.start >= 0 &&
                segment.end <= editable.length &&
                segment.start < segment.end
            ) {

                editable.setSpan(
                    ForegroundColorSpan(segment.color),
                    segment.start,
                    segment.end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun mergeSegments() {

        segments.sortBy { it.start }

        val merged = mutableListOf<ColorRangeUi>()

        for (segment in segments) {

            val last = merged.lastOrNull()

            if (
                last != null &&
                last.end == segment.start &&
                last.color == segment.color
            ) {

                merged[merged.lastIndex] = last.copy(
                    end = segment.end
                )

            } else {

                merged.add(segment)
            }
        }

        segments = merged
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
                    setSpan(
                        ForegroundColorSpan(color),
                        inicioActual,
                        finActual,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    inicioActual = s
                    finActual = e
                }
            }
            // Pintamos el último bloque consolidado
            setSpan(
                ForegroundColorSpan(color),
                inicioActual,
                finActual,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
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