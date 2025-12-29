package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.Model.prueba.UIMovingEvent
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.FragmentWithoutFilesViewModel
import com.jonathanev.review.UI.ViewModel.Fragments.MainToolbarViewModel
import com.jonathanev.review.databinding.FragmentWithoutFilesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentWithoutFiles : Fragment() {
    private var _binding: FragmentWithoutFilesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FragmentWithoutFilesViewModel by viewModels()
    private val viewModelToolbar: MainToolbarViewModel by activityViewModels()

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

        val mode = BundleCompat.getParcelable(
            requireArguments(),
            "mode",
            FolderAction::class.java
        ) ?: FolderAction.None

        initUI(mode)
        initListeners()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsMovingFiles.collect { message ->
                    when (message) {
                        is UIMovingEvent.ShowMessage -> {
                            Toast.makeText(
                                requireContext(),
                                message.text,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModelToolbar.onCancel.collect {
                        viewModelToolbar.initButtons()
                        viewModel.setMainPath()
                        viewModel.moveFileCancel()

                        findNavController().navigate(
                            R.id.action_to_content_graph,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.fragmentsContent, inclusive = true)
                                .build()
                        )
                    }
                }

                launch {
                    viewModelToolbar.onSuccess.collect {
                        viewModelToolbar.initButtons()
                        viewModel.movingFiles(mode)
                        viewModel.setMainPath()
                        viewModel.moveFileSuccess()

                        findNavController().navigate(
                            R.id.action_to_content_graph,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.fragmentsContent, inclusive = true)
                                .build()
                        )
                    }
                }
            }
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

    private fun initListeners() {
        binding.btnAddGuide.setOnClickListener {
            findNavController().navigate(
                R.id.action_to_create_graph,
                bundleOf("mode" to FolderAction.CreatingFile)
            )
        }
    }

    private fun initUI(mode: FolderAction) {
        if (mode is FolderAction.MovingFile) {
            viewModelToolbar.isBtnCancelVisible(View.VISIBLE)
            viewModelToolbar.isBtnSuccessVisible(View.VISIBLE)
        }
    }
}