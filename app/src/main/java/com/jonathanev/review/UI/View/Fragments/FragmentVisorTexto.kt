package com.jonathanev.review.UI.View.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jonathanev.review.presentation.model.ColorRange
import com.jonathanev.review.presentation.model.QuestionContent
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
            QuestionContent.Text::class.java
        ) ?: QuestionContent.None

        initUI(data)
    }

    private fun initUI(questionContent: QuestionContent) {
        when(questionContent){
            is QuestionContent.Image -> Unit
            QuestionContent.None -> Unit
            is QuestionContent.Text -> {
                val builder = questionContent.toSpannable(questionContent.text, questionContent.colorRanges)
                binding.lblText.text = builder
            }
        }
    }

    private fun QuestionContent.Text.toSpannable(
        text: String,
        colorRanges: List<ColorRange>
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)

        for (colorRange in colorRanges) {
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