package com.jonathanev.review.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.R
import com.jonathanev.review.databinding.FragmentListGuidesBinding
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.presentation.event.GuideActionEvent
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.viewmodel.FragmentListGuidesViewModel
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import com.jonathanev.review.presentation.viewmodel.MainToolbarViewModel
import com.jonathanev.review.ui.adapter.ListGuidesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentListGuides : Fragment() {
    private var _binding: FragmentListGuidesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<FragmentListGuidesViewModel>()
    private val navStateViewModel: MainActivityViewModel by activityViewModels()
    private val viewModelToolbar: MainToolbarViewModel by activityViewModels()
    private lateinit var adaptListGuides: ListGuidesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListGuidesBinding.inflate(inflater, container, false)
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
        initListeners(mode)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsMovingFiles.collect { event ->
                    when (event) {
                        UIMovingEvent.ExistFile -> {
                            alertDialog { confirmed ->
                                val relativeGuidePath = RelativeGuidePath(navStateViewModel.guidesPath.value)
                                viewModel.onContinueProcess(confirmed, relativeGuidePath)

                                if (!confirmed) {
                                    showToast("Se ha cancelado la acción")
                                }

                                viewModelToolbar.initButtons()
                                navStateViewModel.setMainPath()
                                findNavController().navigate(
                                    R.id.fragmentsContent,
                                    null,
                                    NavOptions.Builder()
                                        .setPopUpTo(R.id.fragmentsContent, inclusive = true)
                                        .build()
                                )
                            }
                        }

                        is UIMovingEvent.ShowMessage -> {
                            showToast(event.text)

                            viewModelToolbar.initButtons()
                            navStateViewModel.setMainPath()
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
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModelToolbar.onCancel.collect {
                        viewModelToolbar.initButtons()
                        navStateViewModel.setMainPath()
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
                        val relativeGuidePath = RelativeGuidePath(navStateViewModel.guidesPath.value)
                        viewModel.movingGuide(relativeGuidePath)
                        /*viewModelToolbar.initButtons()
                        navStateViewModel.setMainPath()
                        findNavController().navigate(
                            R.id.action_to_content_graph,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.fragmentsContent, inclusive = true)
                                .build()
                        )*/
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsMessages.collect { event ->
                    when (event) {
                        is GuideActionEvent.ShowMessage -> {
                            showToast(event.text)
                        }

                        is GuideActionEvent.Success -> {
                            navStateViewModel.setMainPath()
                            showToast(event.text)

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
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navStateViewModel.back()

                    findNavController().popBackStack(
                        R.id.fragmentListFolders,
                        false
                    )
                }
            }
        )

        viewModel.guides.observe(viewLifecycleOwner) { guides ->
            adaptListGuides.submitList(guides)
        }
    }

    private fun initListeners(mode: FolderAction) {
        binding.btnCreateGuide.setOnClickListener {
            if (mode is FolderAction.MovingFile) {
                showToast("Termina de mover la guia antes de realizar otra acción")
            } else {
                findNavController().navigate(
                    R.id.action_to_create_graph,
                    bundleOf("mode" to FolderAction.CreatingFile)
                )
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(
            requireContext(), text, Toast.LENGTH_LONG
        ).show()
    }

    private fun initUI(mode: FolderAction) {
        viewModelToolbar.changeTitle("Guias")

        if (mode is FolderAction.MovingFile) {
            viewModelToolbar.isBtnCancelVisible(true)
            viewModelToolbar.isBtnSuccessVisible(true)
        }

        adaptListGuides = ListGuidesAdapter { position -> showGuideOptions(position, mode) }
        binding.lvGuiasEstudioNew.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.lvGuiasEstudioNew.setHasFixedSize(true)
        binding.lvGuiasEstudioNew.adapter = adaptListGuides

        val relativeGuidePath = RelativeGuidePath(navStateViewModel.guidesPath.value)
        viewModel.getAllGuides(relativeGuidePath)
    }

    private fun showGuideOptions(position: Int, mode: FolderAction) {
        if (mode is FolderAction.MovingFile) {
            Toast.makeText(
                requireContext(),
                "Termina de mover la guia antes de realizar otra acción",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        when (val guideResult = viewModel.getGuideSelected(position)) {
            is GuideResultUi.Error -> showToast("No se encontró la guia en la posición $position")

            is GuideResultUi.Success -> {
                val builder = AlertDialog.Builder(context)
                builder.setIcon(R.drawable.ic_advertencia)
                builder.setTitle("¿Qué acción deseas realizar?")
                builder.setItems(
                    arrayOf<CharSequence>(
                        "Abrir",
                        "Eliminar",
                        "Cambiar nombre",
                        "Mover",
                        "Cancelar"
                    )
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            //viewModel.openFilePath(guideResult.guideUiModel.nameGuide)
                            val bundle = Bundle().apply {
                                putString("guideId", guideResult.guideUiModel.nameGuide)
                            }

                            findNavController().navigate(
                                R.id.action_to_preview,
                                bundle
                            )
                        }

                        1 ->
                            // Se ejecuta cuando quiere eliminar la guía.
                            AlertDialog.Builder(context)
                                .setTitle("¡Atención!")
                                .setMessage(
                                    "¿Estás seguro que deseas eliminar la" +
                                            " guia?"
                                )
                                .setPositiveButton("Si") { _, _ ->
                                    val relativeGuidePath = RelativeGuidePath(navStateViewModel.guidesPath.value)
                                    viewModel.deleteGuide(guideResult.guideUiModel.nameGuide, relativeGuidePath)
                                }
                                .setNegativeButton("Cancelar") { _, _ -> dialog.dismiss() }
                                .create().show()

                        2 -> {
                            findNavController().navigate(
                                R.id.action_to_create_graph,
                                bundleOf("mode" to FolderAction.RenamingFile(guideResult.guideUiModel.nameGuide))
                            )
                        }

                        3 -> {
                            val relativeGuidePath = RelativeGuidePath(navStateViewModel.guidesPath.value)
                            viewModel.setContext(relativeGuidePath)
                            navStateViewModel.setMainPath()
                            findNavController().navigate(
                                R.id.action_to_content_graph,
                                bundleOf("mode" to FolderAction.MovingFile)
                            )
                        }

                        4 -> {
                            // Cuando cancela se ejecuta esta acción
                            dialog.dismiss()
                            Toast.makeText(
                                context,
                                "Cancelaste la acción",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
                builder.create().show()
            }
        }
    }

    private fun alertDialog(onResult: (Boolean) -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("¡Atención!")
            .setMessage(
                ("Ya tienes una guia con el mismo nombre, " +
                        "si continúas se va a sobreescribir el archivo, " +
                        "¿seguro deseas continuar?")
            )
            .setPositiveButton("Continuar") { _, _ ->
                onResult(true)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                onResult(false)
            }
            .setOnCancelListener {
                onResult(false)
            }
            .create()
            .show()
    }
}