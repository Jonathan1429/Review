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
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.presentation.files.model.GuideResultUi
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.presentation.viewmodel.FragmentListGuidesViewModel
import com.jonathanev.review.presentation.viewmodel.MainToolbarViewModel
import com.jonathanev.review.ui.adapter.ListGuidesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentListGuides : Fragment() {
    private var _binding: FragmentListGuidesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<FragmentListGuidesViewModel>()
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
                        //viewModel.setMainPath()
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
                        //viewModel.movingFiles(mode)
                        //viewModel.setMainPath()
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

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsMessages.collect { event ->
                    if (event is UIStopEvent.ShowMessage) {
                        Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                    }

                    if (event is UIStopEvent.DeleteGuideSuccess) {
                        Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()

                        findNavController().navigate(
                            R.id.fragmentsContent,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.fragmentsContent, true)
                                .build()
                        )
                    }
                }
            }
        }

        /* otra manera de ejecuta el botón para atrás
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { // Si no consumes el evento, puedes volver atrás en la pila de Fragments. // Para esto, deshabilita y llama a la implementación por defecto.
                viewModel.back()

                isEnabled = true
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        // El 'this' como LifecycleOwner asegura que el callback se maneje correctamente // con el ciclo de vida del Fragment.

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)*/

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.back()

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

    private fun initListeners() {
        binding.btnCreateGuide.setOnClickListener {
            findNavController().navigate(
                R.id.action_to_create_graph,
                bundleOf("mode" to FolderAction.CreatingFile)
            )
        }
    }

    private fun initUI(mode: FolderAction) {
        viewModelToolbar.changeTitle("Guias")

        if (mode is FolderAction.MovingFile) {
            viewModelToolbar.isBtnCancelVisible(View.VISIBLE)
            viewModelToolbar.isBtnSuccessVisible(View.VISIBLE)
        }

        adaptListGuides = ListGuidesAdapter { position -> showGuideOptions(position, mode) }
        binding.lvGuiasEstudioNew.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.lvGuiasEstudioNew.setHasFixedSize(true)
        binding.lvGuiasEstudioNew.adapter = adaptListGuides

        viewModel.getAllGuides()
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
            is GuideResultUi.Error -> Toast.makeText(
                requireContext(),
                "No se pudo cargar la guia",
                Toast.LENGTH_SHORT
            ).show()

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

                            Toast.makeText(
                                requireContext(),
                                "No se pueden eliminar archivos aún",
                                Toast.LENGTH_SHORT
                            ).show()
                        // Se ejecuta cuando quiere eliminar la guía.
                        /*AlertDialog.Builder(context)
                            .setTitle("¡Atención!")
                            .setMessage(
                                "¿Estás seguro que deseas eliminar la" +
                                        " guia?"
                            )
                            .setPositiveButton("Si") { _, _ ->
                                //viewModel.deleteFiles(guideResult.guideUiModel.nameGuide)
                            }
                            .setNegativeButton("Cancelar") { _, _ -> dialog.dismiss() }
                            .create().show()*/

                        2 -> {
                            findNavController().navigate(
                                R.id.action_to_create_graph,
                                bundleOf("mode" to FolderAction.RenamingFile(guideResult.guideUiModel.nameGuide))
                            )
                        }

                        3 -> {
                            Toast.makeText(
                                requireContext(),
                                "No se puede eliminar aún",
                                Toast.LENGTH_SHORT
                            ).show()

                            /*val filePath =
                                viewModel.getFilePath(guideResult.guideUiModel.nameGuide)
                            viewModel.changeFilePathToMain()

                            findNavController().navigate(
                                R.id.action_to_content_graph,
                                bundleOf("mode" to FolderAction.MovingFile(filePath))
                            )*/
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
}