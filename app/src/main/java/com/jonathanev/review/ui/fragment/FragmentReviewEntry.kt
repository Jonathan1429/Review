package com.jonathanev.review.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.R
import com.jonathanev.review.databinding.FragmentReviewEntryBinding
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.presentation.viewmodel.FragReviewEntryViewModel
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class FragmentReviewEntry : Fragment() {
    private var _binding: FragmentReviewEntryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FragReviewEntryViewModel by viewModels()
    private val navStateViewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = BundleCompat.getParcelable(
            requireArguments(),
            "mode",
            FolderAction::class.java
        ) ?: FolderAction.None

        initUI(mode)
    }

    private fun initUI(mode: FolderAction) {
        val relativeGuidePath = RelativeGuidePath(navStateViewModel.guidesPath.value)
        val noGuides = viewModel.getGuides(relativeGuidePath)
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.fragmentReviewEntry, true)
            .build()

        if (noGuides > 0) {
            findNavController().navigate(
                resId = R.id.action_fragmentReviewEntry_to_fragmentListGuides2,
                args = bundleOf("mode" to mode),
                navOptions = navOptions
            )

            // setPopUpTo elimina los fragments que se encuentren entre el que se ponga ahí y la nueva
            // navegación, si hay un true se elimina el que se puso en el popUp, si es un false no se borra.
            /*--- Pila ANTES de navegar desde Entry ---
            [0] content_graph
            [1] fragmentsContent
            [2] fragmentListFolders
            --------------------------
            --- Pila en Entry sin ejecutar popUp ---
            [0] content_graph
            [1] fragmentsContent
            [2] fragmentListFolders
            [3] review_graph
            [4] fragmentReviewEntry
            --------------------------
            --- Pila en ListGuides ---
            [0] content_graph
            [1] fragmentsContent
            [2] fragmentListFolders
            [3] review_graph
            [4] fragmentListGuides2
            --------------------------
             */
        } else {
            findNavController().navigate(
                R.id.action_fragmentReviewEntry_to_fragmentWithoutFiles2,
                bundleOf("mode" to mode),
                navOptions
            )
        }
    }
}