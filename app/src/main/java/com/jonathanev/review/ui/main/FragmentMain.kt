package com.jonathanev.review.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.R
import com.jonathanev.review.databinding.FragmentMainActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentMain : Fragment() {
    private var _binding: FragmentMainActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCrearCarpeta.setOnClickListener {
            findNavController().navigate(
                R.id.action_to_create_graph,
                bundleOf("mode" to FolderAction.CreatingFolder)
            )
        }
    }
}