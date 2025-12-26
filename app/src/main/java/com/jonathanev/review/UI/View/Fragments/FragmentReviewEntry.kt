package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.R
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
        val noGuides = viewModel.getGuides()

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.fragmentReviewEntry, true)
            .build()

        if (noGuides > 0) {
            findNavController().navigate(
                R.id.action_fragmentReviewEntry_to_fragmentListGuides2,
                null,
                navOptions
            )
        } else {
            findNavController().navigate(
                R.id.action_fragmentReviewEntry_to_fragmentWithoutFiles2,
                null,
                navOptions
            )
        }
    }
}