package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.Model.ScreenData
import com.jonathanev.review.databinding.FragmentCreateFileBinding

class FragmentCreateFile : Fragment() {
    private var _binding: FragmentCreateFileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = BundleCompat.getParcelable(
            requireArguments(), "mode", FolderAction::class.java
        ) ?: FolderAction.NONE

        val screenData = BundleCompat.getParcelable(
            requireArguments(), "screenData", ScreenData::class.java
        ) ?: ScreenData("", "", 0, 0)

        if (1 == 1){

        }
    }
}