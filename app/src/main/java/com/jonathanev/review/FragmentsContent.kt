package com.jonathanev.review

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.data.FolderAction
import com.jonathanev.review.UI.ViewModel.FragmentsContentViewModel
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

        viewModel.folders.observe(viewLifecycleOwner){ folders ->
            if (folders.isEmpty()){
                findNavController().navigate(
                    R.id.action_to_empty
                )
            } else {
                findNavController().navigate(
                    R.id.action_to_list,
                    bundleOf("mode" to mode)
                )
                if (mode is FolderAction.MovingFile){
                    Log.i("Moviendo: ", mode.pathFile.path)
                }
            }
        }

        initUI()
    }

    private fun initUI() {
        viewModel.getAllFolders()
    }
}