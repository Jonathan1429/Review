package com.jonathanev.review.UI.View.Fragments

import android.app.AlertDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.data.FolderAction
import com.jonathanev.review.data.Model.GuideResult
import com.jonathanev.review.data.Model.prueba.UIMovingEvent
import com.jonathanev.review.data.Model.prueba.UIStopEvent
import com.jonathanev.review.Fragments.Adaptadores.ListGuidesAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.FragmentListGuidesViewModel
import com.jonathanev.review.UI.ViewModel.Fragments.MainToolbarViewModel
import com.jonathanev.review.databinding.FragmentListGuidesBinding
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
                    when(message){
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

        if (mode is FolderAction.MovingFile){
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

        val guideResult = viewModel.getGuideSelected(position)

        when (guideResult) {
            is GuideResult.Error -> Log.i("Error", "Error")
            is GuideResult.Success -> {
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
                            viewModel.changeFilePath(guideResult.folder.nameGuide)
                            findNavController().navigate(
                                R.id.action_to_preview,
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
                                    viewModel.deleteFiles(guideResult.folder.nameGuide)
                                }
                                .setNegativeButton("Cancelar") { _, _ -> dialog.dismiss() }
                                .create().show()

                        2 -> {
                            viewModel.changeFilePath(guideResult.folder.nameGuide)

                            findNavController().navigate(
                                R.id.action_to_create_graph,
                                bundleOf("mode" to FolderAction.RenamingFile)
                            )
                        }

                        3 -> {
                            val filePath = viewModel.getFilePath(guideResult.folder.nameGuide)
                            viewModel.changeFilePathToMain()

                            findNavController().navigate(
                                R.id.action_to_content_graph,
                                bundleOf("mode" to FolderAction.MovingFile(filePath))
                            )

                            Log.i("Moviendo: ", filePath.path)
                        }

                        4 -> {
                            // Cuando cancela se ejecuta esta acción
                            dialog.dismiss()
                            Toast.makeText(context, "Cancelaste la acción", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
                builder.create().show()
            }
        }
    }
}