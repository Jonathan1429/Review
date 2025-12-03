package com.jonathanev.review.UI.View.Fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.Model.prueba.UiStopEvent
import com.jonathanev.review.Fragments.Adaptadores.ListItemPintarImagenesAdapter
import com.jonathanev.review.Fragments.Adaptadores.ListItemPintarTextosAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.FragmentRepasarViewModel
import com.jonathanev.review.databinding.FragmentRepasarBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentRepasar : Fragment() {
    private var _binding: FragmentRepasarBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<FragmentRepasarViewModel>()

    private lateinit var adaptListPintarTextos: ListItemPintarTextosAdapter
    private lateinit var adaptListPintarImagenes: ListItemPintarImagenesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRepasarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val positionContent = arguments?.getInt(
            "posContent"
        ) ?: 0


        initUI(positionContent)
        listeners()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsMessages.collect { event ->
                    when (event) {
                        is UiStopEvent.ShowMessage -> {
                            AlertDialog.Builder(requireContext())
                                .setTitle("¡Atención!")
                                .setMessage(event.text)
                                .setPositiveButton(
                                    "Si"
                                ) { _, _ ->
                                    viewModel.restartReview()

                                    Toast.makeText(
                                        requireContext(), "Guia reiniciada", Toast.LENGTH_LONG
                                    ).show()
                                }
                                .setNegativeButton(
                                    "Cancelar"
                                ) { dialog, _ ->
                                    dialog.dismiss()
                                }.setOnCancelListener {

                                }.create().show()

                            return@collect
                        }

                        is UiStopEvent.NotQuestionBefore -> {
                            Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                if (uiState.shouldFlip) {
                    girarCardView()
                }

                adaptListPintarTextos.submitList(uiState.textList)
                adaptListPintarImagenes.submitList(uiState.imageList)
            }
        }

        viewModel.typeContent.observe(viewLifecycleOwner) { typeContent ->
            binding.lblPregResp.text =
                if (typeContent == TypeContent.QUESTION) "Pregunta" else "Respuesta"
        }
    }

    private fun listeners() {
        binding.imgvPregResp.setOnClickListener {
            viewModel.swapTypeContent()
        }

        binding.imgvNext.setOnClickListener {
            viewModel.nextQuestion()
        }

        binding.imgvPrevious.setOnClickListener {
            viewModel.beforeQuestion()
        }
    }

    private fun initUI(positionContent: Int) {
        adaptListPintarTextos = ListItemPintarTextosAdapter { position -> goVisorTexto(position) }
        binding.recyclerTextos.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerTextos.setHasFixedSize(true)
        binding.recyclerTextos.adapter = adaptListPintarTextos

        adaptListPintarImagenes =
            ListItemPintarImagenesAdapter { position -> goVisorImagen(position) }
        binding.recyclerImagenes.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerImagenes.setHasFixedSize(true)
        binding.recyclerImagenes.adapter = adaptListPintarImagenes
        //viewModel.setContadorPregunta(positionContent)
        viewModel.getObtenerDatosXML(positionContent)
    }

    private fun goVisorTexto(position: Int) {
        findNavController().navigate(
            R.id.action_fragmentRepasar_to_fragmentVisorTexto,
            bundleOf("questionText" to viewModel.uiState.value.textList[position])
        )
    }

    private fun goVisorImagen(position: Int) {
        findNavController().navigate(
            R.id.action_fragmentRepasar_to_fragmentVisorImagen,
            bundleOf("questionImage" to viewModel.uiState.value.imageList[position])
        )
    }

    private fun girarCardView() {
        var flipAnimatorTexts =
            ObjectAnimator.ofFloat(binding.flContTexts, "rotationY", 0f, 180f)
        flipAnimatorTexts.duration = 0 // Duración de la animación en milisegundos
        flipAnimatorTexts.start()
        flipAnimatorTexts.doOnEnd {
            //showImageOrText()
            flipAnimatorTexts =
                ObjectAnimator.ofFloat(binding.flContTexts, "rotationY", 180f, 0f)
            flipAnimatorTexts.duration = 1000 // Duración de la animación en milisegundos
            flipAnimatorTexts.start()
        }

        var flipAnimatorImages =
            ObjectAnimator.ofFloat(binding.flContImages, "rotationY", 0f, 180f)
        flipAnimatorImages.duration = 0 // Duración de la animación en milisegundos
        flipAnimatorImages.start()
        flipAnimatorImages.doOnEnd {
            //showImageOrText()
            flipAnimatorImages =
                ObjectAnimator.ofFloat(binding.flContImages, "rotationY", 180f, 0f)
            flipAnimatorImages.duration = 1000 // Duración de la animación en milisegundos
            flipAnimatorImages.start()
        }
    }
}