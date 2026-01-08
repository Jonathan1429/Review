package com.jonathanev.review.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.R
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.presentation.viewmodel.FragmentsContentViewModel
import com.jonathanev.review.databinding.FragmentFragmentsContentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentsContent : Fragment() {
    private var _binding: FragmentFragmentsContentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FragmentsContentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFragmentsContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = BundleCompat.getParcelable(
            requireArguments(),
            "mode",
            FolderAction::class.java
        ) ?: FolderAction.None

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.createEntryFragment, true)
            .build()

        viewModel.folders.observe(viewLifecycleOwner){ folders ->
            if (folders.isEmpty()){
                findNavController().navigate(
                    resId = R.id.action_to_empty,
                    args = null,
                    navOptions = navOptions
                )
            } else {
                findNavController().navigate(
                    resId = R.id.action_to_list,
                    args = bundleOf("mode" to mode),
                    navOptions = navOptions
                )
            }
        }

        initUI()
    }

    private fun initUI() {
        viewModel.getAllFolders()
    }
}