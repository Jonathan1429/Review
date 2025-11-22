package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jonathanev.review.R
import com.jonathanev.review.databinding.FragmentCreateFile2Binding
import com.jonathanev.review.databinding.FragmentCreateFilesBinding

class FragmentCreateFile : Fragment() {
    private var _binding: FragmentCreateFile2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCreateFile2Binding.inflate(inflater, container, false)
        return binding.root
    }
}