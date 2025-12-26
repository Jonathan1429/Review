package com.jonathanev.review.UI.View.Fragments

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
import com.jonathanev.review.Data.ActionGuide
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.Model.ScreenData
import com.jonathanev.review.Data.Model.prueba.UICreatingFile
import com.jonathanev.review.Data.Model.prueba.UIStopEvent
import com.jonathanev.review.Fragments.Adaptadores.ListarIconosAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.FragCreateFilesViewModel
import com.jonathanev.review.databinding.FragmentCreateFilesBinding
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
    private val viewModel: FragCreateFilesViewModel by viewModels()
    private var mode: FolderAction = FolderAction.NONE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateFilesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animación cuando se esté seleccionando un color.
        val bubbleFlag = BubbleFlag(context)
        bubbleFlag.flagMode = FlagMode.FADE
        binding.fragmentCreate.colorPickerView.flagView = bubbleFlag

        mode = BundleCompat.getParcelable(
            requireArguments(),
            "mode",
            FolderAction::class.java
        ) ?: FolderAction.NONE

        initUI()
        initListeners()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { values ->
                    when (values) {
                        is UICreatingFile.ContinuedProcess -> {
                            folderAction(values.name, values.description)
                        }

                        is UICreatingFile.Message -> Toast.makeText(
                            context,
                            values.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        is UICreatingFile.FileExisted -> {

                        }
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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Cargar íconos en el adapter si cambia la lista
                    iconsAdapter.submitList(state.icons)
                    iconsAdapter.handleItemClick(state.selectedIndex)

                    // Actualizar preview
                    binding.fragmentCreate.prevCarpeta.ivCarpeta.setImageResource(state.icons[state.selectedIndex])
                    val background =
                        binding.fragmentCreate.prevCarpeta.bgCarpeta.background as GradientDrawable
                    binding.fragmentCreate.prevCarpeta.ivCarpeta.imageTintMode =
                        PorterDuff.Mode.SRC_ATOP
                    val color50 = ColorUtils.setAlphaComponent(state.color, 50)
                    background.setColor(color50)

                    binding.fragmentCreate.prevCarpeta.ivCarpeta.imageTintList =
                        ColorStateList.valueOf(state.color)
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

    private fun initUI() {
        // 1) crear adapter una vez
        iconsAdapter = ListarIconosAdapter { pos -> viewModel.onIconSelected(pos) }
        binding.fragmentCreate.rvIconos.adapter = iconsAdapter
        binding.fragmentCreate.rvIconos.layoutManager =
            GridLayoutManager(requireContext(), 6)

        viewModel.loadIconsFor(mode)

        when (mode) {
            FolderAction.CREATING_FOLDER -> showFolderUI()
            FolderAction.RENAMING_FILE -> {
                showFileUI()
                val attributes = viewModel.fillFields()
                binding.fragmentCreate.etNombre.setText(attributes.nameGuide)
                binding.fragmentCreate.fragmentComponentsFile.etDescription.setText(attributes.description)
            }

            FolderAction.RENAMING_FOLDER -> showFolderUI()
            FolderAction.CREATING_FILE -> showFileUI()
            FolderAction.NONE -> Log.e("Error", "No se pudieron cargar datos iniciales")
        }
    }

    private fun initListeners() {
        binding.fragmentCreate.colorPickerView.setColorListener(ColorListener { color, _ ->
            viewModel.setColor(color)
        })

        binding.btnApply.setOnClickListener {
            val name = binding.fragmentCreate.etNombre.text.toString()
            val description =
                binding.fragmentCreate.fragmentComponentsFile.etDescription.text.toString()

            prepareScreenData(name, description)
        }
    }

    private fun prepareScreenData(name: String, description: String) {
        val exist = viewModel.fileExist(mode, name)

        if (!exist) {
            viewModel.processScreenData(name, description)
            return
        }

        when (mode) {
            FolderAction.CREATING_FOLDER,
            FolderAction.RENAMING_FOLDER -> {
                Toast.makeText(
                    requireContext(),
                    "Ya tienes una carpeta con el mismo nombre",
                    Toast.LENGTH_SHORT
                ).show()
            }

            FolderAction.CREATING_FILE,
            FolderAction.RENAMING_FILE -> {
                alertDialog { confirmed ->
                    viewModel.onContinueProcess(confirmed, name, description)
                }
            }

            FolderAction.NONE -> return
        }
    }

    private fun folderAction(name: String, description: String) {
        val state = viewModel.uiState.value

        val data = ScreenData(
            name = name,
            description = description,
            imgFolder = state.icons[state.selectedIndex],
            color = state.color
        )

        when (mode) {
            FolderAction.CREATING_FOLDER -> onCreateFolderConfirmed(data)
            FolderAction.RENAMING_FILE -> renameFile()
            FolderAction.RENAMING_FOLDER -> Log.i(
                "Advertencia",
                "Aun no se aplica la funcion renombrar folder"
            )

            FolderAction.CREATING_FILE -> onCreateGuideConfirmed(data)
            FolderAction.NONE -> Log.e("Error", "No se pudo crear el archivo")
        }
    }

    private fun onCreateGuideConfirmed(data: ScreenData) {
        findNavController().navigate(
            R.id.action_to_create_file,
            bundleOf(
                "mode" to mode,
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