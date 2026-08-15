package com.jonathanev.review.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.viewmodel.FragmentsContentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentsContent : Fragment(R.layout.fragment_compose_container) {
    private val viewModel: FragmentsContentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val composeView = view.findViewById<ComposeView>(R.id.composeView)

        // Termina el ciclo de vida correctamente en Compose
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

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
        //viewModel.getAllFolders()
    }
}