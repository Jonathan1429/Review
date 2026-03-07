package com.jonathanev.review.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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

        // Termina el ciclo de vida correctamente en Compose
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        val window = requireActivity().window

        composeView.setContent {
            val isDark = isSystemInDarkTheme()

            // Controla los iconos de la status bar
            val controller = WindowCompat.getInsetsController(window, window.decorView)

            SideEffect {
                controller.isAppearanceLightStatusBars = !isDark
            }

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