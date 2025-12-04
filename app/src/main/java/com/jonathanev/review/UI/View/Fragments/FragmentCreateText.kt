package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jonathanev.review.R
import com.jonathanev.review.databinding.FragmentCreateFilesBinding
import com.jonathanev.review.databinding.FragmentCreateTextBinding

class FragmentCreateText : Fragment() {
    private var _binding: FragmentCreateTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}