package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.FragmentWithoutFilesViewModel
import com.jonathanev.review.databinding.FragmentWithoutFilesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentWithoutFiles : Fragment() {
    private var _binding: FragmentWithoutFilesBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<FragmentWithoutFilesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentWithoutFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddGuide.setOnClickListener {
            findNavController().navigate(
                R.id.action_to_create_graph,
                bundleOf("mode" to FolderAction.CREATING_FILE)
            )
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.setMainPath()

                // Si no consumes el evento, puedes volver atrás en la pila de Fragments.
                // Para esto, deshabilita y llama a la implementación por defecto.
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        // El 'this' como LifecycleOwner asegura que el callback se maneje correctamente
        // con el ciclo de vida del Fragment.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}