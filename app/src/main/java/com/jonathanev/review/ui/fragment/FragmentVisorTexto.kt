package com.jonathanev.review.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.databinding.FragmentVisorTextoBinding
import com.jonathanev.review.presentation.model.ColorRangeUi
import com.jonathanev.review.presentation.model.QuestionContentUi

class FragmentVisorTexto : Fragment() {
    private var _binding: FragmentVisorTextoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisorTextoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arguments?.getParcelable(
            "questionText",
            QuestionContentUi.Text::class.java
        ) ?: QuestionContentUi.None

        initUI(data)
    }

    private fun initUI(questionContentUi: QuestionContentUi) {
        when(questionContentUi){
            is QuestionContentUi.Image -> Unit
            QuestionContentUi.None -> Unit
            is QuestionContentUi.Text -> {
                val builder = questionContentUi.toSpannable(questionContentUi.text, questionContentUi.colorRanges)
                binding.lblText.text = builder
            }
        }
    }

    private fun QuestionContentUi.Text.toSpannable(
        text: String,
        colorRangeUi: List<ColorRangeUi>
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)

        for (colorRange in colorRangeUi) {
            val colorSpan = ForegroundColorSpan(colorRange.color)
            builder.setSpan(
                colorSpan,
                colorRange.start,
                colorRange.end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return builder
    }
}