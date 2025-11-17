package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.FragCreateFolderViewModel
import com.jonathanev.review.UI.ViewModel.Fragments.Fragment_DialogColoresMod_popupViewModel
import com.jonathanev.review.databinding.FragmentCreateFolderBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentCreateFolder : Fragment() {
    private lateinit var binding:FragmentCreateFolderBinding
    private val viewModel: FragCreateFolderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateFolderBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}