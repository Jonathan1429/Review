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
import com.jonathanev.review.domain.model.ColorRangeDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.databinding.FragmentVisorTextoBinding

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
            QuestionContentDomain.Text::class.java
        ) ?: QuestionContentDomain.None

        initUI(data)
    }

    private fun initUI(questionContentDomain: QuestionContentDomain) {
        when(questionContentDomain){
            is QuestionContentDomain.Image -> Unit
            QuestionContentDomain.None -> Unit
            is QuestionContentDomain.Text -> {
                val builder = questionContentDomain.toSpannable(questionContentDomain.text, questionContentDomain.colorRangeDomains)
                binding.lblText.text = builder
            }
        }
    }

    private fun QuestionContentDomain.Text.toSpannable(
        text: String,
        colorRangeDomains: List<ColorRangeDomain>
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)

        for (colorRange in colorRangeDomains) {
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