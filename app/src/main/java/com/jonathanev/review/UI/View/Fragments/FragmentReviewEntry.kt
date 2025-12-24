package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.jonathanev.review.UI.ViewModel.Fragments.FragReviewEntryViewModel
import com.jonathanev.review.databinding.FragmentReviewEntryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentReviewEntry : Fragment() {
    private var _binding: FragmentReviewEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FragReviewEntryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {
        viewModel.getGuides()
    }
}