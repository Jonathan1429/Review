package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                adaptListPintarTextos.submitList(uiState.textList)
                adaptListPintarImagenes.submitList(uiState.imageList)
            }
        }
    }

    private fun initUI(positionContent: Int) {
        adaptListPintarTextos = ListItemPintarTextosAdapter { position -> goVisorTexto(position) }
        binding.recyclerTextos.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerTextos.setHasFixedSize(true)
        binding.recyclerTextos.adapter = adaptListPintarTextos

        adaptListPintarImagenes = ListItemPintarImagenesAdapter { position -> goVisorImagen(position) }
        binding.recyclerImagenes.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
}