package com.jonathanev.review.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.data.filesystem.FilePathsProviderImpl
import com.jonathanev.review.ui.adapter.ListFoldersAdapter
import com.jonathanev.review.R
import com.jonathanev.review.presentation.viewmodel.FoldersListViewModel
import com.jonathanev.review.presentation.viewmodel.MainToolbarViewModel
import com.jonathanev.review.databinding.FragmentListFoldersBinding
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.presentation.event.FolderActionEvent
import com.jonathanev.review.presentation.model.FolderResultUi
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentListFolders : DialogFragment() {
    private var _binding: FragmentListFoldersBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<FoldersListViewModel>()
    private val viewModelToolbar: MainToolbarViewModel by activityViewModels()
    private val navStateViewModel: MainActivityViewModel by activityViewModels()

    private lateinit var adaptListFolders: ListFoldersAdapter

    @Inject
    lateinit var filePathsProviderImpl: FilePathsProviderImpl

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListFoldersBinding.inflate(inflater, container, false)
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
                viewModel.eventsMessages.collect { event ->
                    when (event) {
                        FolderActionEvent.DeleteFolderSuccess -> {
                            viewModel.getAllFolders()
                            showToast("Se ha borrado la carpeta correctamente")
                        }

                        is FolderActionEvent.ShowMessage -> showToast(event.text)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foldersUiState.collect { uiState ->
                    binding.progressBar.isVisible = uiState.isLoading

                    if (uiState.error != null) {
                        showToast(uiState.error)
                    }

                    adaptListFolders.submitList(uiState.folders)
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsMovingFiles.collect { message ->
                    when (message) {
                        is UIMovingEvent.ShowMessage -> {
                            showToast(message.text)
                        }

                        UIMovingEvent.ExistFile -> {
                            showToast("No es posible guardar una guia aquí")
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
                        viewModel.moveFileCancel()

                        findNavController().navigate(
                            findNavController().graph.startDestinationId,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(findNavController().graph.id, inclusive = true)
                                .build()
                        )
                    }
                }
            }
        }
    }

    private fun initListeners(mode: FolderAction) {
        binding.btnCreateGuide.setOnClickListener {
            if (mode is FolderAction.MovingFile) {
                showToast("Termina de mover la guia antes de realizar otra acción")
            } else {
                findNavController().navigate(
                    R.id.action_to_create_graph,
                    bundleOf("mode" to FolderAction.CreatingFolder)
                )
            }
        }
    }

    private fun initUI(mode: FolderAction) {
        if (mode is FolderAction.MovingFile) {
            viewModelToolbar.isBtnCancelVisible(true)
            viewModelToolbar.isBtnSuccessVisible(false)
        }

        viewModelToolbar.changeTitle("Carpetas")

        binding.progressBar.visibility = View.VISIBLE

        adaptListFolders = ListFoldersAdapter { position -> showFolderOptions(position, mode) }
        binding.lvGuiasEstudioNew.layoutManager = GridLayoutManager(context, 2)
        binding.lvGuiasEstudioNew.setHasFixedSize(true)
        binding.lvGuiasEstudioNew.adapter = adaptListFolders

        viewModel.getAllFolders()
    }

    @SuppressLint("SdCardPath")
    private fun showFolderOptions(position: Int, mode: FolderAction) {
        //val a = guiasViewModel.guias.value
        //var fileClickeado: File = filePathsProvider.fileGuides

        when (val folderResult = viewModel.getFolderSelected(position)) {
            //GuiaResult.Empty -> Log.i("Vacio", "Vacio")
            is FolderResultUi.Error -> Log.i("Error", "Error")
            is FolderResultUi.Success -> {
                val builder = AlertDialog.Builder(context)
                builder.setIcon(R.drawable.ic_advertencia)
                builder.setTitle("¿Qué acción deseas realizar?")
                builder.setItems(
                    arrayOf<CharSequence>(
                        "Abrir",
                        "Eliminar",
                        "Cancelar"
                    )
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            navStateViewModel.next(folderResult.folderUi.folder.name)

                            findNavController().navigate(
                                R.id.action_to_review_graph,
                                bundleOf("mode" to mode)
                            )
                        }

                        1 -> {
                            // Se ejecuta cuando quiere eliminar la carpeta.
                            AlertDialog.Builder(context)
                                .setTitle("¡Atención!")
                                .setMessage(
                                    "¿Estás seguro que deseas eliminar la carpeta y su contenido?"
                                )
                                .setPositiveButton("Si") { _, _ ->
                                    viewModel.deleteFiles(folderResult.folderUi.folder.name)
                                }
                                .setNegativeButton("Cancelar") { _, _ -> dialog.dismiss() }
                                .create().show()
                        }

                        2 -> {
                            // Cuando cancela se ejecuta esta acción
                            dialog.dismiss()
                            showToast("Cancelaste la acción")
                        }
                    }
                }
                builder.create().show()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}