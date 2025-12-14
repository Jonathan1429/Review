package com.jonathanev.review.UI.View.Fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.davemorrissey.labs.subscaleview.ImageSource
import com.jonathanev.review.Data.Model.prueba.QuestionContent
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
            QuestionContent.Image::class.java
        ) ?: QuestionContent.None

        initUI(data)
    }

    private fun initUI(questionContent: QuestionContent) {
        when(questionContent){
            is QuestionContent.Image -> {
                binding.ivImagen.setImage(ImageSource.uri(questionContent.uri))
            }
            QuestionContent.None -> Unit
            is QuestionContent.Text -> Unit
        }
    }
}