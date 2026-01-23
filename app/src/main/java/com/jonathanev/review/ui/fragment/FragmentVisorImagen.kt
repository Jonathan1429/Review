package com.jonathanev.review.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.jonathanev.review.databinding.FragmentVisorImagenBinding
import com.jonathanev.review.presentation.model.QuestionContentUi

class FragmentVisorImagen : Fragment() {
    private var _binding: FragmentVisorImagenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisorImagenBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arguments?.getParcelable(
            "questionImage",
            QuestionContentUi.Image::class.java
        ) ?: QuestionContentUi.None

        initUI(data)
    }

    private fun initUI(questionContentUi: QuestionContentUi) {
        when(questionContentUi){
            is QuestionContentUi.Image -> {
                binding.ivImagen.setImage(ImageSource.uri(questionContentUi.uri))
            }
            QuestionContentUi.None -> Unit
            is QuestionContentUi.Text -> Unit
        }
    }
}