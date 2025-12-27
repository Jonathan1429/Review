package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
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
import com.jonathanev.review.Data.ActionGuide
import com.jonathanev.review.Data.Model.ScreenData
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.Model.prueba.UIStopEvent
import com.jonathanev.review.Fragments.Adaptadores.ListCreateImagesAdapter
import com.jonathanev.review.Fragments.Adaptadores.ListCreateTextsAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.MainToolbarViewModel
import com.jonathanev.review.UI.ViewModel.Fragments.SharedFragmentCreateFileViewModel
import com.jonathanev.review.databinding.FragmentCreateFileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentCreateFile : Fragment() {
    private var _binding: FragmentCreateFileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedFragmentCreateFileViewModel by activityViewModels()
    private val viewModelToolbar: MainToolbarViewModel by activityViewModels()

    private lateinit var adaptListCreateTexts: ListCreateTextsAdapter
    private lateinit var adaptListCreateImages: ListCreateImagesAdapter
    private lateinit var actionGuide: ActionGuide
    private lateinit var screenData: ScreenData

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

        actionGuide = BundleCompat.getParcelable(
            requireArguments(), "actionGuide", ActionGuide::class.java
        ) ?: ActionGuide.NONE

        screenData = BundleCompat.getParcelable(
            requireArguments(), "screenData", ScreenData::class.java
        ) ?: ScreenData("", "", 0, 0)

        /*val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (mode == FolderAction.RENAMING_FILE) {
                    viewModel.beforePath()
                }

                // back real
                findNavController().navigateUp()
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, callback)*/

        initUI()
        initListeners()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStopEvent.collect { uiStopEvent ->
                    if (uiStopEvent is UIStopEvent.NotQuestionBefore) {
                        Toast.makeText(
                            requireContext(),
                            uiStopEvent.text,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (uiStopEvent is UIStopEvent.ShowMessage) {
                        Toast.makeText(
                            requireContext(),
                            uiStopEvent.text,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (uiStopEvent is UIStopEvent.GuideCreatedSuccess) {
                        findNavController().navigate(
                            R.id.fragmentsContent,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.fragmentsContent, true)
                                .build()
                        )

                        Toast.makeText(context, uiStopEvent.text, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    binding.lblPregResp.text =
                        if (uiState.typeContent == TypeContent.QUESTION)
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

    private fun initUI() {
        viewModelToolbar.isBtnSaveVisible(View.GONE)
        viewModelToolbar.isBtnBackVisible(View.GONE)

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

        when (actionGuide) {
            ActionGuide.CREATE -> Log.i("Crear", "Se está creando un archivo")
            is ActionGuide.EDIT -> {
                viewModel.getObtenerDatosXML((actionGuide as ActionGuide.EDIT).posGuide)
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

    private fun initListeners() {
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

        binding.btnSaveGuide.setOnClickListener {
            when (actionGuide) {
                ActionGuide.CREATE -> viewModel.saveNewGuide(
                    screenData.name,
                    screenData.description
                )

                is ActionGuide.EDIT -> viewModel.saveOldGuide()
                ActionGuide.NONE -> Log.e("Error:", "NO se pudo guardar la guia")
            }

        }
    }
}