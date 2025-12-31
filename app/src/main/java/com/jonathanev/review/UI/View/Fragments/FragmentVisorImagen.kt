package com.jonathanev.review.UI.View.Fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.davemorrissey.labs.subscaleview.ImageSource
import com.jonathanev.review.presentation.model.QuestionContentDomain
import com.jonathanev.review.databinding.FragmentVisorImagenBinding

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
            QuestionContentDomain.Image::class.java
        ) ?: QuestionContentDomain.None

        initUI(data)
    }

    private fun initUI(questionContentDomain: QuestionContentDomain) {
        when(questionContentDomain){
            is QuestionContentDomain.Image -> {
                binding.ivImagen.setImage(ImageSource.uri(questionContentDomain.uri))
            }
            QuestionContentDomain.None -> Unit
            is QuestionContentDomain.Text -> Unit
        }
    }
}