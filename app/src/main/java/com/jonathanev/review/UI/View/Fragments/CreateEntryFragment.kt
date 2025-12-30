package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.data.ActionGuide
import com.jonathanev.review.data.FolderAction
import com.jonathanev.review.R

class CreateEntryFragment : Fragment(R.layout.fragment_fragments_content) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = BundleCompat.getParcelable(
            requireArguments(),
            "mode",
            FolderAction::class.java
        ) ?: FolderAction.None

        val actionGuide = BundleCompat.getParcelable(
            requireArguments(),
            "actionGuide",
            ActionGuide::class.java
        ) ?: ActionGuide.NONE

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.createEntryFragment, true)
            .build()

        if (actionGuide is ActionGuide.EDIT) {
            findNavController().navigate(
                R.id.action_createEntryFragment_to_fragmentCreateFile,
                bundleOf("actionGuide" to actionGuide),
                navOptions
            )
        } else {
            findNavController().navigate(
                R.id.action_createEntryFragment_to_fragmentCreateFiles,
                bundleOf(
                    "mode" to mode,
                    "actionGuide" to actionGuide
                ),
                navOptions
            )
        }
    }
}

