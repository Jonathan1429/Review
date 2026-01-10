package com.jonathanev.review.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.R
import com.jonathanev.review.databinding.FragmentPreviewQuestionsBinding
import com.jonathanev.review.presentation.model.ActionGuide
import com.jonathanev.review.presentation.viewmodel.FragmentRepasarViewModel
import com.jonathanev.review.ui.adapter.ListPreviewQuestionsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentPreviewQuestions : Fragment() {
    private var _binding: FragmentPreviewQuestionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FragmentRepasarViewModel by activityViewModels()
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

        val folderId = arguments?.getString(
            "guideId"
        ) ?: ""

        initUI(folderId)
        initListeners(folderId)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //viewModel.setMainPath()

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
            viewModel.uiStatePreview.collect { uiStatePreview ->
                adaptListPreviewQuestion.submitList(uiStatePreview.previewState)
            }
        }
    }

    private fun initListeners(folderId: String) {
        binding.btnAddQuestions.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Ya puedes agregar más preguntas",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigate(
                R.id.action_to_create_graph,
                bundleOf("actionGuide" to ActionGuide.EDIT(folderId, -1))
            )
        }
    }

    private fun initUI(folderId: String) {
        adaptListPreviewQuestion = ListPreviewQuestionsAdapter(
            clickedPlay = { position -> goReview(position) },
            clickedEdit = { position -> goEdit(position, folderId) })
        binding.reciclerPreviewQuestions.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.reciclerPreviewQuestions.setHasFixedSize(true)
        binding.reciclerPreviewQuestions.adapter = adaptListPreviewQuestion

        viewModel.uploadCachedGuides()
        viewModel.getObtenerDatosXML(folderId)
    }

    private fun goEdit(position: Int, folderId: String) {
        findNavController().navigate(
            R.id.action_to_create_graph,
            bundleOf("actionGuide" to ActionGuide.EDIT(folderId, position))
        )
    }

    private fun goReview(position: Int) {
        viewModel.showQuestionClicked(position)

        findNavController().navigate(
            R.id.action_fragmentPreviewQuestions_to_fragmentRepasar,
        )
    }
}