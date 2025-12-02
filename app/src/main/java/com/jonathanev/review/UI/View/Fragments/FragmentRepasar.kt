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

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                adaptListPintarTextos.submitList(uiState.textList)
            }
        }
    }

    private fun initUI() {
        adaptListPintarTextos = ListItemPintarTextosAdapter { position -> goVisor(position) }
        binding.recyclerTextos.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerTextos.setHasFixedSize(true)
        binding.recyclerTextos.adapter = adaptListPintarTextos

        viewModel.getObtenerDatosXML()
    }

    private fun goVisor(position: Int) {
        findNavController().navigate(
            R.id.action_fragmentRepasar_to_fragmentVisorTexto,
            bundleOf("questionText" to viewModel.uiState.value.textList[position])
        )

        /*findNavController().navigate(
            R.id.action_fragmentMainActivity_to_fragmentCreateFiles,
            bundleOf("mode" to FolderAction.CREATING_FOLDER)
        )*/
    }
}