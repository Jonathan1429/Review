package com.jonathanev.review.UI.View.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.jonathanev.review.data.FolderAction
import com.jonathanev.review.data.FolderResult
import com.jonathanev.review.presentation.event.UIMovingEvent
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.Fragments.Adaptadores.ListFoldersAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.FragDialListarFoldersViewModel
import com.jonathanev.review.UI.ViewModel.Fragments.MainToolbarViewModel
import com.jonathanev.review.databinding.FragmentListFoldersBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentListFolders : DialogFragment() {
    private var _binding: FragmentListFoldersBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<FragDialListarFoldersViewModel>()
    private val viewModelToolbar: MainToolbarViewModel by activityViewModels()

    private lateinit var adaptListFolders: ListFoldersAdapter

    @Inject
    lateinit var filePathsProvider: FilePathsProvider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var mode = BundleCompat.getParcelable(
            requireArguments(),
            "mode",
            FolderAction::class.java
        ) ?: FolderAction.None

        initUI(mode)
        initListeners()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsMessages.collect { event ->
                    if (event is UIStopEvent.ShowMessage) {
                        Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                    }

                    if (event is UIStopEvent.DeleteFolderSuccess) {
                        Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()

                        viewModel.getAllFolders()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foldersUiState.collect { uiState ->
                    binding.progressBar.isVisible = uiState.isLoading

                    if (uiState.error != null) {
                        Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_LONG).show()
                    }

                    adaptListFolders.submitList(uiState.folders)
                }
            }
        }

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

        viewModel.file.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.VISIBLE
            //guiasViewModel.getAllUpdatedGuides(it)
        }
    }

    private fun initListeners() {
        binding.btnCreateGuide.setOnClickListener {
            findNavController().navigate(
                R.id.action_to_create_graph,
                bundleOf("mode" to FolderAction.CreatingFolder)
            )
        }
    }

    private fun initUI(mode: FolderAction) {
        if (mode is FolderAction.MovingFile) {
            viewModelToolbar.isBtnCancelVisible(View.VISIBLE)
            viewModelToolbar.isBtnSuccessVisible(View.GONE)
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
        val folderResult = viewModel.getFolderSelected(position)
        //val a = guiasViewModel.guias.value
        //var fileClickeado: File = filePathsProvider.fileGuides

        when (folderResult) {
            //GuiaResult.Empty -> Log.i("Vacio", "Vacio")
            is FolderResult.Error -> Log.i("Error", "Error")
            is FolderResult.Success -> {
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
                            viewModel.changeFilePath(folderResult.folder.folderUiModel.name)
                            findNavController().navigate(
                                R.id.action_to_review_graph,
                                bundleOf("mode" to mode)
                            )
                        }

                        1 -> {
                            // Se ejecuta cuando quiere eliminar la guía.
                            AlertDialog.Builder(context)
                                .setTitle("¡Atención!")
                                .setMessage(
                                    "¿Estás seguro que deseas eliminar la carpeta y su contenido?"
                                )
                                .setPositiveButton("Si") { _, _ ->
                                    viewModel.deleteFiles(folderResult.folder)
                                }
                                .setNegativeButton("Cancelar") { _, _ -> dialog.dismiss() }
                                .create().show()
                        }

                        2 -> {
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val metrics = resources.displayMetrics

            val maxWidth = 900   // en píxeles
            val maxHeight = 1200 // en píxeles

            val calculatedWidth = (metrics.widthPixels * 0.9).toInt()  // 90% del ancho de pantalla
            val calculatedHeight = (metrics.heightPixels * 0.8).toInt() // 80% del alto de pantalla

            val finalWidth = minOf(calculatedWidth, maxWidth)
            val finalHeight = minOf(calculatedHeight, maxHeight)

            window.setLayout(finalWidth, finalHeight)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}