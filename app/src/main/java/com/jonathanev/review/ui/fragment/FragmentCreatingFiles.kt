package com.jonathanev.review.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.presentation.model.ActionGuide
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.model.GuideResultUi
import com.jonathanev.review.presentation.model.ScreenDataUi
import com.jonathanev.review.presentation.viewmodel.CreateFilesViewModel
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import com.jonathanev.review.ui.mapper.toNav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentCreatingFiles : Fragment(R.layout.fragment_compose_container) {
    private val viewModel: CreateFilesViewModel by viewModels()
    private val navStateViewModel: MainActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val composeView = view.findViewById<ComposeView>(R.id.composeView)

        // Termina el ciclo de vida correctamente en Compose
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        val mode = BundleCompat.getParcelable(
            requireArguments(),
            "mode",
            FolderAction::class.java
        ) ?: FolderAction.None

        // Esto estaba descomentado
        /*when (mode) {
            // Esto estaba descomentado
            FolderAction.CreatingFolder -> onCreateFolderConfirmed(data)
            is FolderAction.RenamingFile -> renameFile(mode.fileName)
            FolderAction.RenamingFolder -> Log.i(
                "Advertencia",
                "Aun no se aplica la funcion renombrar folder"
            )

            FolderAction.CreatingFile -> OnCreateGuideConfirmed(data)
            FolderAction.None -> Log.e("Error", "No se pudo crear el archivo")
            is FolderAction.MovingFile -> Log.i("Moviendo: ", "Moviendo archivos")
        }*/

        // Esto estaba descomentado
        /*composeView.setContent {
            ReviewTheme {
                PropertiesFiles(
                    mode = mode
                )
                /*onCreateFolderClick = {
                findNavController().navigate(
                    R.id.action_to_create_graph,
                    bundleOf("mode" to FolderAction.CreatingFolder)
                )
            }*/


            }
        }*/

        // Animación cuando se esté seleccionando un color.
        /*val bubbleFlag = BubbleFlag(context)
        bubbleFlag.flagMode = FlagMode.FADE
        binding.fragmentCreate.colorPickerView.setInitialColor(Color.WHITE)
        binding.fragmentCreate.colorPickerView.flagView = bubbleFlag*/

        initUI(mode)
        //initListeners(mode)

        /*lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { values ->
                    when (values) {
                        is CreatingFileUiState.ContinuedProcess -> {
                            FolderAction(mode, values.name, values.description)
                        }

                        is CreatingFileUiState.Message -> Toast.makeText(
                            context,
                            values.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }*/

        /*lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsMessages.collect { event ->
                    navStateViewModel.setMainPath()

                    when (event) {
                        is RenameGuideEvent.ShowMessage -> {
                            showToast(event.message)

                            findNavController().navigate(
                                R.id.fragmentsContent,
                                null,
                                NavOptions.Builder()
                                    .setPopUpTo(R.id.content_graph, true) // Limpia el historial
                                    .build()
                            )
                        }
                    }
                }
            }
        }*/

        /*lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Cargar íconos en el adapter si cambia la lista
                    val drawableIcons = state.icons.map { it.toDrawableRes() }

                    iconsAdapter.submitList(drawableIcons)
                    iconsAdapter.handleItemClick(state.selectedIndex)

                    // Actualizar preview
                    /*binding.fragmentCreate.prevCarpeta.ivCarpeta.setImageResource(drawableIcons[state.selectedIndex])
                    val background =
                        binding.fragmentCreate.prevCarpeta.bgCarpeta.background as GradientDrawable
                    binding.fragmentCreate.prevCarpeta.ivCarpeta.imageTintMode =
                        PorterDuff.Mode.SRC_ATOP*/

                    val isDark = requireContext().isDarkTheme()

                    val color = when (state.color) {
                        ColorType.Black -> Color.BLACK
                        ColorType.Gray -> Color.GRAY
                        ColorType.White -> Color.WHITE
                        is ColorType.RandomColor -> state.color.color
                        ColorType.Default -> if (isDark) Color.WHITE else Color.BLACK
                    }

                    /*val color50 = ColorUtils.setAlphaComponent(color, 50)
                    background.setColor(color50)

                    binding.fragmentCreate.prevCarpeta.ivCarpeta.imageTintList =
                        ColorStateList.valueOf(color)*/
                }
            }
        }*/
    }

    /*fun Context.isDarkTheme(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK

        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }*/

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
        //iconsAdapter = ListarIconosAdapter { pos -> viewModel.onIconSelected(pos) }
        /*binding.fragmentCreate.rvIconos.adapter = iconsAdapter
        binding.fragmentCreate.rvIconos.layoutManager =
            GridLayoutManager(requireContext(), 6)*/

        //viewModel.loadIconsFor(mode)
        val relativeGuidePath = RelativeGuidePath(navStateViewModel.guidesPath.value)

        when (mode) {
            FolderAction.CreatingFolder -> showToast("Sección implementada")//showFolderUI()
            is FolderAction.RenamingFile -> {
                //showFileUI()
                viewModel.uploadCachedGuides(relativeGuidePath)

                when (val result = viewModel.fillFields(mode.fileName)) {
                    is GuideResultUi.Error -> showToast("No se ha encontrado la guia a cargar")
                    is GuideResultUi.Success -> {
                        /*binding.fragmentCreate.etNombre.setText(result.guideUiModel.nameGuide)
                        binding.fragmentCreate.fragmentComponentsFile.etDescription.setText(result.guideUiModel.description)*/
                    }
                }
            }

            FolderAction.RenamingFolder -> showToast("Sección aún no implementada 2") //showFolderUI()
            FolderAction.CreatingFile -> {
                showToast("Sección aún no implementada3")
                //showFileUI()
                viewModel.uploadCachedGuides(relativeGuidePath)
            }

            FolderAction.None -> Log.e("Error", "No se pudieron cargar datos iniciales")
            is FolderAction.MovingFile -> Log.i("Moviendo: ", "Moviendo archivos")
        }
    }

    /*private fun initListeners(mode: FolderAction) {
        binding.fragmentCreate.colorPickerView.setColorListener(ColorListener { color, _ ->
            viewModel.setColor(color)
        })

        binding.btnAplicar.setOnClickListener {
            val name = binding.fragmentCreate.etNombre.text.toString().trim()
            val description =
                binding.fragmentCreate.fragmentComponentsFile.etDescription.text.toString().trim()

            prepareScreenData(mode, name, description)
        }
    }*/

    /*private fun prepareScreenData(mode: FolderAction, name: String, description: String) {
        val isExistFile = viewModel.fileExist(mode, name)

        if (!isExistFile) {
            viewModel.validateData(name, description)
            return
        }

        when (mode) {
            FolderAction.CreatingFolder,
            FolderAction.RenamingFolder -> {
                showToast("Ya tienes una carpeta con el mismo nombre")
            }

            FolderAction.CreatingFile,
            is FolderAction.RenamingFile -> {
                alertDialog { confirmed ->
                    viewModel.onContinueProcess(confirmed, name, description)
                }
            }

            FolderAction.None -> return
            is FolderAction.MovingFile -> Log.i("Moviendo: ", "Moviendo archivos")
        }
    }*/

    /*@Composable
    private fun FolderAction(mode: FolderAction, name: String, description: String) {
        val state = viewModel.uiState.value

        val icon = state.icons[state.selectedIndex]

        val data = ScreenDataUi(
            name = name,
            description = description,
            imgFolder = icon,
            color = state.color
        )

        when (mode) {
            FolderAction.CreatingFolder -> onCreateFolderConfirmed(data)
            is FolderAction.RenamingFile -> renameFile(mode.fileName)
            FolderAction.RenamingFolder -> Log.i(
                "Advertencia",
                "Aun no se aplica la funcion renombrar folder"
            )

            FolderAction.CreatingFile -> OnCreateGuideConfirmed(data)
            FolderAction.None -> Log.e("Error", "No se pudo crear el archivo")
            is FolderAction.MovingFile -> Log.i("Moviendo: ", "Moviendo archivos")
        }
    }*/

    @Composable
    private fun OnCreateGuideConfirmed(data: ScreenDataUi) {
        val isDark = isSystemInDarkTheme()

        findNavController().navigate(
            R.id.action_to_create_file,
            bundleOf(
                //"mode" to mode,
                "screenData" to data.toNav(isDark),
                "actionGuide" to ActionGuide.CREATE
            )
        )
    }

    private fun onCreateFolderConfirmed(data: ScreenDataUi) {
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

    private fun renameFile(oldName: String) {
        /*val fileName = binding.fragmentCreate.etNombre.text.toString().trim()
        val description =
            binding.fragmentCreate.fragmentComponentsFile.etDescription.text.toString().trim()*/

        val relativeGuidePath = RelativeGuidePath(navStateViewModel.guidesPath.value)
        //viewModel.renameFile(oldName, fileName, description, relativeGuidePath)
    }

    // ---------------------------
    // Funciones de UI
    // ---------------------------
    /*private fun showFolderUI() {
        binding.fragmentCreate.fragmentComponentsFile.root.visibility = View.GONE
    }*/

    /*private fun showFileUI() {
        binding.fragmentCreate.fragmentComponentsFile.root.visibility = View.GONE
        binding.fragmentCreate.lblPickColor.visibility = View.GONE
        binding.fragmentCreate.colorPickerView.visibility = View.GONE
        binding.fragmentCreate.lblPreview.visibility = View.GONE
        binding.fragmentCreate.prevCarpeta.root.visibility = View.GONE

        binding.fragmentCreate.lblSelectIcon.text = getString(R.string.lblIcon)
        binding.fragmentCreate.lblNameYourFolder.text = getString(R.string.lblNameYourFile)
        binding.fragmentCreate.fragmentComponentsFile.root.visibility = View.VISIBLE
    }*/

    /*override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }*/

    private fun showToast(text: String) {
        Toast.makeText(
            requireContext(), text, Toast.LENGTH_LONG
        ).show()
    }
}