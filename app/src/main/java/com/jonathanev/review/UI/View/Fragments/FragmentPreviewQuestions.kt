package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.Data.ActionGuide
import com.jonathanev.review.Fragments.Adaptadores.ListPreviewQuestionsAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.FragmentPreviewQuestionsViewModel
import com.jonathanev.review.databinding.FragmentPreviewQuestionsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentPreviewQuestions : Fragment() {
    private var _binding: FragmentPreviewQuestionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FragmentPreviewQuestionsViewModel by viewModels()
    private lateinit var adaptListPreviewQuestion: ListPreviewQuestionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewQuestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()

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

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                Log.i("UISTATE", "Lista recibida: ${uiState.previewState.size}")
                adaptListPreviewQuestion.submitList(uiState.previewState)
            }
        }
    }

    private fun initUI() {
        adaptListPreviewQuestion = ListPreviewQuestionsAdapter(
            clickedPlay = { position -> goReview(position) },
            clickedEdit = { position -> goEdit(position) })
        binding.reciclerPreviewQuestions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.reciclerPreviewQuestions.setHasFixedSize(true)
        binding.reciclerPreviewQuestions.adapter = adaptListPreviewQuestion

        viewModel.getObtenerDatosXML()
    }

    private fun goEdit(position: Int) {
        findNavController().navigate(
            R.id.action_fragmentPreviewQuestions_to_fragmentCreateFile2,
            bundleOf("actionGuide" to ActionGuide.EDIT(position))
        )
    }

    private fun goReview(position: Int) {
        val bundle = Bundle().apply {
            putInt("posContent", position)
        }

        findNavController().navigate(
            R.id.action_fragmentPreviewQuestions_to_fragmentRepasar,
            bundle
        )
    }
}