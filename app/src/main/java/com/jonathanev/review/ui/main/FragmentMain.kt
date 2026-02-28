package com.jonathanev.review.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.ui.screens.MainScreen
import com.jonathanev.review.ui.theme.ReviewTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentMain : Fragment(R.layout.fragment_compose_container) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val composeView = view.findViewById<ComposeView>(R.id.composeView)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false)

        composeView.setContent {
            val isDark = isSystemInDarkTheme()

            // Controla los iconos de la status bar
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = !isDark

            ReviewTheme(darkTheme = isDark) {
                MainScreen(
                    onCreateFolderClick = {
                        findNavController().navigate(
                            R.id.action_to_create_graph,
                            bundleOf("mode" to FolderAction.CreatingFolder)
                        )
                    }
                )

            }
        }
    }
}