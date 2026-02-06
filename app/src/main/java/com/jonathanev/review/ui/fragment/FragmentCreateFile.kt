package com.jonathanev.review.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jonathanev.review.R
import com.jonathanev.review.databinding.FragmentCreateFileBinding
import com.jonathanev.review.domain.model.QAType
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.presentation.event.CreateGuideEvent
import com.jonathanev.review.presentation.model.ActionGuide
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import com.jonathanev.review.presentation.viewmodel.MainToolbarViewModel
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.adapter.ListCreateImagesAdapter
import com.jonathanev.review.ui.adapter.ListCreateTextsAdapter
import com.jonathanev.review.ui.model.ScreenDataNav
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentCreateFile : Fragment() {
    private var _binding: FragmentCreateFileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedFragmentCreateFileViewModel by activityViewModels()
    private val navStateViewModel: MainActivityViewModel by activityViewModels()
    private val viewModelToolbar: MainToolbarViewModel by activityViewModels()

    private lateinit var adaptListCreateTexts: ListCreateTextsAdapter
    private lateinit var adaptListCreateImages: ListCreateImagesAdapter
    private lateinit var screenDataNav: ScreenDataNav

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*val mode = BundleCompat.getParcelable(
            requireArguments(), "mode", FolderAction::class.java
        ) ?: FolderAction.NONE*/

        val actionGuide = BundleCompat.getParcelable(
            requireArguments(), "actionGuide", ActionGuide::class.java
        ) ?: ActionGuide.NONE

        screenDataNav = BundleCompat.getParcelable(
            requireArguments(), "screenData", ScreenDataNav::class.java
        ) ?: ScreenDataNav("", "", R.drawable.ic_anchor_solid_full, R.color.black)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.initUIState()
                findNavController().popBackStack()
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, callback)

        initUI(actionGuide)
        initListeners(actionGuide)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createGuideEvent.collect { createGuideEvent ->
                    when (createGuideEvent) {
                        CreateGuideEvent.AddMoreQuestions -> {
                            AlertDialog.Builder(requireContext())
                                .setTitle("¡Atención!")
                                .setMessage("Ya no hay mas preguntas, ¿quieres agregar mas?")
                                .setPositiveButton(
                                    "Si"
                                ) { _, _ ->
                                    viewModel.updateLastQuestion()
                                    //viewModel.nextQuestion()
                                }
                                .setNegativeButton(
                                    "Cancelar"
                                ) { dialog, _ ->
                                    dialog.dismiss()
                                }.setOnCancelListener {

                                }.create().show()
                        }

                        CreateGuideEvent.ErrorGuideCreated -> showToast("No se pudo crear la guia")
                        CreateGuideEvent.NotQuestionBefore -> showToast("Ya no hay preguntas anteriores")
                        is CreateGuideEvent.ShowMessage -> showToast(createGuideEvent.text)
                        CreateGuideEvent.SuccessGuideCreated -> {
                            viewModel.initUIState()
                            showToast("Guia creada correctamente")

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
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    binding.lblPregResp.text =
                        if (uiState.qAType == QAType.QUESTION)
                            getString(R.string.etPregunta)
                        else
                            getString(R.string.etRespuesta)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observamos imageList directamente
                launch {
                    viewModel.imageList.collect { list ->
                        adaptListCreateImages.submitList(list)
                    }
                }

                // Observamos textList directamente
                launch {
                    viewModel.textList.collect { list ->
                        adaptListCreateTexts.submitList(list)
                    }
                }
            }
        }
    }

    private fun initUI(actionGuide: ActionGuide) {
        viewModelToolbar.isBtnSaveVisible(false)
        viewModelToolbar.isBtnBackVisible(false)

        adaptListCreateTexts = ListCreateTextsAdapter(
            onEditClicked = { position -> goEditText(position) },
            onDeleteClicked = { position -> goDeleteText(position) }
        )
        binding.recyclerTextos.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerTextos.setHasFixedSize(true)
        binding.recyclerTextos.adapter = adaptListCreateTexts

        adaptListCreateImages =
            ListCreateImagesAdapter(
                onEditClicked = { position -> goEditImage(position) },
                onDeleteClicked = { position -> goDeleteImage(position) }
            )
        binding.recyclerImagenes.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerImagenes.setHasFixedSize(true)
        binding.recyclerImagenes.adapter = adaptListCreateImages

        val relativeGuidePath = RelativeGuidePath(navStateViewModel.guidesPath.value)
        when (actionGuide) {
            ActionGuide.CREATE -> Log.i("Crear", "Se está creando un archivo")
            is ActionGuide.EDIT -> {
                viewModel.getObtenerDatosXML(actionGuide.posGuide, actionGuide.nameGuide, relativeGuidePath)
            }

            ActionGuide.NONE -> {
                Log.e("Error", "No se pudo realizar alguna acción")
            }
        }
    }

    private fun goDeleteText(position: Int) {
        viewModel.deleteText(position)
    }

    private fun goDeleteImage(position: Int) {
        viewModel.deleteImage(position)
    }

    private fun goEditImage(position: Int) {
        viewModel.setEditingMode(true, position)

        findNavController().navigate(
            R.id.action_to_images,
            bundleOf("questionImage" to viewModel.imageList.value[position])
        )
    }

    private fun goEditText(position: Int) {
        viewModel.setEditingMode(true, position)

        findNavController().navigate(
            R.id.action_to_text,
            bundleOf("questionText" to viewModel.textList.value[position])
        )
    }

    private fun initListeners(actionGuide: ActionGuide) {
        binding.btnAddText.setOnClickListener {
            findNavController().navigate(
                R.id.action_to_text
            )
        }

        binding.btnAddImages.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("posImage", -1)
            }

            findNavController().navigate(
                R.id.action_to_images,
                bundle
            )
        }

        binding.btnPregResp.setOnClickListener {
            viewModel.rollPregResp()
        }

        binding.btnPrevious.setOnClickListener {
            viewModel.previousQuestion()
        }

        binding.btnNext.setOnClickListener {
            viewModel.nextQuestion()
        }

        binding.btnTrash.setOnClickListener {
            lifecycleScope.launch {
                val dontAsk = viewModel.getDontAskDeleteOnce()
                if (dontAsk) {
                    viewModel.deleteQuesAns()
                } else {
                    showDeletePopup()
                }
            }
        }

        binding.btnSaveGuide.setOnClickListener {
            val relativeGuidePath = RelativeGuidePath(navStateViewModel.guidesPath.value)

            when (actionGuide) {
                ActionGuide.CREATE -> viewModel.saveNewGuide(
                    nameGuide = screenDataNav.name,
                    description = screenDataNav.description,
                    relativeGuidePath = relativeGuidePath
                )

                is ActionGuide.EDIT -> {
                    viewModel.saveOldGuide(
                        nameGuide = actionGuide.nameGuide,
                        relativeGuidePath = relativeGuidePath
                    )
                }

                ActionGuide.NONE -> Log.e("Error:", "NO se pudo guardar la guia")
            }
        }
    }

    private fun showDeletePopup() {
        val dialogView = layoutInflater.inflate(
            R.layout.pop_up_confirmar_accion, // <-- este XML que mandaste
            null
        )

        // Referencias a vistas
        val btnConfirmar =
            dialogView.findViewById<MaterialButton>(R.id.btnConfirmar)
        val btnCancelar =
            dialogView.findViewById<MaterialButton>(R.id.btnCancelar)
        val switchElection =
            dialogView.findViewById<SwitchCompat>(R.id.switchElection)

        // Crear el dialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Cancelar
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        // Confirmar
        btnConfirmar.setOnClickListener {
            if (switchElection.isChecked) {
                viewModel.saveDontAskDelete()
            }

            viewModel.deleteQuesAns()
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()
    }

    private fun showToast(text: String) {
        Toast.makeText(
            requireContext(), text, Toast.LENGTH_LONG
        ).show()
    }
}