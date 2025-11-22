package com.jonathanev.review

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.UI.ViewModel.FragmentsContentViewModel
import com.jonathanev.review.UI.ViewModel.MainActivityViewModel
import com.jonathanev.review.databinding.FragmentFragmentsContentBinding
import com.jonathanev.review.databinding.FragmentMainActivityBinding
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

        viewModel.guias.observe(viewLifecycleOwner){ guides ->
            if (guides.isEmpty()){
                findNavController().navigate(
                    R.id.action_fragmentsContent_to_fragmentMainActivity
                )
            } else {
                findNavController().navigate(
                    R.id.action_fragmentsContent_to_fragmentDialogListarGuiasPopup,
                )
            }
        }

        initUI()
    }

    private fun initUI() {
        viewModel.getAllGuias()
    }
}