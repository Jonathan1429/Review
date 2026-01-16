package com.jonathanev.review.ui.fragment

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.domain.model.TypeContent
import com.jonathanev.review.ui.adapter.ListItemPintarImagenesAdapter
import com.jonathanev.review.ui.adapter.ListItemPintarTextosAdapter
import com.jonathanev.review.R
import com.jonathanev.review.presentation.viewmodel.FragmentRepasarViewModel
import com.jonathanev.review.databinding.FragmentRepasarBinding
import com.jonathanev.review.presentation.event.GuideReviewEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentRepasar : Fragment() {
    private var _binding: FragmentRepasarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FragmentRepasarViewModel by activityViewModels()

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

        initUI()
        initListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observamos imageList directamente
                launch {
                    viewModel.imageList.collect { list ->
                        adaptListPintarImagenes.submitList(list)
                    }
                }

                // Observamos textList directamente
                launch {
                    viewModel.textList.collect { list ->
                        adaptListPintarTextos.submitList(list)
                    }
                }

                // Texto pregunta/respuesta
                launch {
                    viewModel.uiState.collect { uiState ->
                        binding.lblPregResp.text =
                            if (uiState.typeContent == TypeContent.QUESTION)
                                getString(R.string.etPregunta)
                            else
                                getString(R.string.etRespuesta)
                    }
                }

                launch {
                    viewModel.eventsMessages.collect { event ->
                        when (event) {
                            GuideReviewEvent.NotQuestionBefore ->
                                showToast("Ya no tienes preguntas anteriores")

                            GuideReviewEvent.RestartGuide -> {
                                AlertDialog.Builder(requireContext())
                                    .setTitle("¡Atención!")
                                    .setMessage("Se acabaron las preguntas, ¿quieres repetir la guia?")
                                    .setPositiveButton(
                                        "Si"
                                    ) { _, _ ->
                                        viewModel.restartReview()
                                        showToast("Guia reiniciada")
                                    }
                                    .setNegativeButton(
                                        "Cancelar"
                                    ) { dialog, _ ->
                                        dialog.dismiss()
                                    }.setOnCancelListener {

                                    }.create().show()
                            }

                            is GuideReviewEvent.ShowMessage -> showToast(event.text)
                        }
                    }
                }
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(
            requireContext(), text, Toast.LENGTH_LONG
        ).show()
    }

    private fun initListeners() {
        binding.btnPregResp.setOnClickListener {
            viewModel.swapTypeContent()
        }

        binding.btnNext.setOnClickListener {
            viewModel.nextQuestion()
        }

        binding.btnPrevious.setOnClickListener {
            viewModel.beforeQuestion()
        }
    }

    private fun initUI() {
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

        //viewModel.uploadCachedGuides()
        //viewModel.getObtenerDatosXML()
    }

    private fun goVisorTexto(position: Int) {
        findNavController().navigate(
            R.id.action_fragmentRepasar_to_fragmentVisorTexto,
            bundleOf("questionText" to viewModel.textList.value[position])
        )
    }

    private fun goVisorImagen(position: Int) {
        findNavController().navigate(
            R.id.action_fragmentRepasar_to_fragmentVisorImagen,
            bundleOf("questionImage" to viewModel.imageList.value[position])
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