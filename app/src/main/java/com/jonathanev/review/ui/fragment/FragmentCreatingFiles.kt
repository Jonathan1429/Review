package com.jonathanev.review.ui.fragment

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.ColorUtils
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.jonathanev.review.presentation.model.ActionGuide
import com.jonathanev.review.presentation.folders.model.FolderAction
import com.jonathanev.review.presentation.model.ScreenData
import com.jonathanev.review.presentation.state.CreatingFileUiState
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.ui.adapter.ListarIconosAdapter
import com.jonathanev.review.R
import com.jonathanev.review.presentation.files.viewmodel.CreateFilesViewModel
import com.jonathanev.review.databinding.FragmentCreateFilesBinding
import com.jonathanev.review.ui.mapper.toInt
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentCreatingFiles : Fragment() {
    private var _binding: FragmentCreateFilesBinding? = null
    private val binding get() = _binding!!
    private lateinit var iconsAdapter: ListarIconosAdapter
    private val viewModel: CreateFilesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateFilesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = BundleCompat.getParcelable(
            requireArguments(),
            "mode",
            FolderAction::class.java
        ) ?: FolderAction.None

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (mode is FolderAction.RenamingFile) {
                    viewModel.beforePath()
                }

                // back real
                findNavController().navigateUp()
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, callback)

        // Animación cuando se esté seleccionando un color.
        val bubbleFlag = BubbleFlag(context)
        bubbleFlag.flagMode = FlagMode.FADE
        binding.fragmentCreate.colorPickerView.flagView = bubbleFlag

        initUI(mode)
        initListeners(mode)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { values ->
                    when (values) {
                        is CreatingFileUiState.ContinuedProcess -> {
                            folderAction(mode, values.name, values.description)
                        }

                        is CreatingFileUiState.Message -> Toast.makeText(
                            context,
                            values.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsMessages.collect { event ->
                    if (event is UIStopEvent.GuideRenamedSuccess) {
                        findNavController().navigate(
                            R.id.fragmentsContent,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.fragmentsContent, true)
                                .build()
                        )

                        Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                    }

                    if (event is UIStopEvent.ShowMessage) {
                        Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Cargar íconos en el adapter si cambia la lista
                    val drawableIcons = state.icons.map { it.toInt() }

                    iconsAdapter.submitList(drawableIcons)
                    iconsAdapter.handleItemClick(state.selectedIndex)

                    // Actualizar preview
                    binding.fragmentCreate.prevCarpeta.ivCarpeta.setImageResource(drawableIcons[state.selectedIndex])
                    val background =
                        binding.fragmentCreate.prevCarpeta.bgCarpeta.background as GradientDrawable
                    binding.fragmentCreate.prevCarpeta.ivCarpeta.imageTintMode =
                        PorterDuff.Mode.SRC_ATOP
                    val color50 = ColorUtils.setAlphaComponent(state.color.toInt(), 50)
                    background.setColor(color50)

                    binding.fragmentCreate.prevCarpeta.ivCarpeta.imageTintList =
                        ColorStateList.valueOf(state.color.toInt())
                }
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

    private fun initUI(mode: FolderAction) {
        // 1) crear adapter una vez
        iconsAdapter = ListarIconosAdapter { pos -> viewModel.onIconSelected(pos) }
        binding.fragmentCreate.rvIconos.adapter = iconsAdapter
        binding.fragmentCreate.rvIconos.layoutManager =
            GridLayoutManager(requireContext(), 6)

        viewModel.loadIconsFor(mode)

        when (mode) {
            FolderAction.CreatingFolder -> showFolderUI()
            is FolderAction.RenamingFile -> {
                showFileUI()
                viewModel.uploadCachedGuides()

                val attributes = viewModel.fillFields(mode.fileName)
                binding.fragmentCreate.etNombre.setText(attributes.nameGuide)
                binding.fragmentCreate.fragmentComponentsFile.etDescription.setText(attributes.description)
            }

            FolderAction.RenamingFolder -> showFolderUI()
            FolderAction.CreatingFile -> {
                showFileUI()
                viewModel.uploadCachedGuides()
            }
            FolderAction.None -> Log.e("Error", "No se pudieron cargar datos iniciales")
            is FolderAction.MovingFile -> Log.i("Moviendo: ", "Moviendo archivos a ${mode.pathFile.path}")
        }
    }

    private fun initListeners(mode: FolderAction) {
        binding.fragmentCreate.colorPickerView.setColorListener(ColorListener { color, _ ->
            viewModel.setColor(color)
        })

        binding.btnAplicar.setOnClickListener {
            val name = binding.fragmentCreate.etNombre.text.toString().trim()
            val description =
                binding.fragmentCreate.fragmentComponentsFile.etDescription.text.toString().trim()

            prepareScreenData(mode, name, description)
        }
    }

    private fun prepareScreenData(mode: FolderAction, name: String, description: String) {
        val isExistFile = viewModel.fileExist(mode, name)

        if (!isExistFile) {
            viewModel.processScreenData(name, description)
            return
        }

        when (mode) {
            FolderAction.CreatingFolder,
            FolderAction.RenamingFolder -> {
                Toast.makeText(
                    requireContext(),
                    "Ya tienes una carpeta con el mismo nombre",
                    Toast.LENGTH_SHORT
                ).show()
            }

            FolderAction.CreatingFile,
            is FolderAction.RenamingFile -> {
                alertDialog { confirmed ->
                    viewModel.onContinueProcess(confirmed, name, description)
                }
            }

            FolderAction.None -> return
            is FolderAction.MovingFile -> Log.i("Moviendo: ", "Moviendo archivos a ${mode.pathFile.path}")
        }
    }

    private fun folderAction(mode: FolderAction, name: String, description: String) {
        val state = viewModel.uiState.value

        val icon = state.icons[state.selectedIndex]
        /*val drawableIcon = when(icon){
            IconType.LIGHTBULB -> R.drawable.ic_lightbulb_solid_full
            IconType.ANCHOR_SOLID_FULL -> R.drawable.ic_anchor_solid_full
            IconType.ANGELLIST_BRANDS_SOLID_FULL -> R.drawable.ic_angellist_brands_solid_full
            IconType.BACTERIA_SOLID_FULL -> R.drawable.ic_bacteria_solid_full
        }*/

        val data = ScreenData(
            name = name,
            description = description,
            imgFolder = icon,
            color = state.color.toInt()
        )

        when (mode) {
            FolderAction.CreatingFolder -> onCreateFolderConfirmed(data)
            is FolderAction.RenamingFile -> renameFile()
            FolderAction.RenamingFolder -> Log.i(
                "Advertencia",
                "Aun no se aplica la funcion renombrar folder"
            )

            FolderAction.CreatingFile -> onCreateGuideConfirmed(data)
            FolderAction.None -> Log.e("Error", "No se pudo crear el archivo")
            is FolderAction.MovingFile -> Log.i("Moviendo: ", "Moviendo archivos a ${mode.pathFile.path}")
        }
    }

    private fun onCreateGuideConfirmed(data: ScreenData) {
        findNavController().navigate(
            R.id.action_to_create_file,
            bundleOf(
                //"mode" to mode,
                "screenData" to data,
                "actionGuide" to ActionGuide.CREATE
            )
        )
    }

    private fun onCreateFolderConfirmed(data: ScreenData) {
        viewModel.saveMetadata(data)

        Toast.makeText(
            requireContext(),
            "Carpeta creada exitosamente",
            Toast.LENGTH_SHORT
        ).show()

        findNavController().navigate(
            R.id.fragmentsContent,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.content_graph, true) // Limpia el historial
                .build()
        )
    }

    private fun renameFile() {
        val fileName = binding.fragmentCreate.etNombre.text.toString()
        val description =
            binding.fragmentCreate.fragmentComponentsFile.etDescription.text.toString()
        viewModel.renameFile(fileName, description)
    }

    // ---------------------------
    // Funciones de UI
    // ---------------------------
    private fun showFolderUI() {
        binding.fragmentCreate.fragmentComponentsFile.root.visibility = View.GONE
    }

    private fun showFileUI() {
        binding.fragmentCreate.fragmentComponentsFile.root.visibility = View.GONE
        binding.fragmentCreate.lblPickColor.visibility = View.GONE
        binding.fragmentCreate.colorPickerView.visibility = View.GONE
        binding.fragmentCreate.lblPreview.visibility = View.GONE
        binding.fragmentCreate.prevCarpeta.root.visibility = View.GONE

        binding.fragmentCreate.lblSelectIcon.text = getString(R.string.lblIcon)
        binding.fragmentCreate.lblNameYourFolder.text = getString(R.string.lblNameYourFile)
        binding.fragmentCreate.fragmentComponentsFile.root.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}