package com.jonathanev.review.UI.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.Model.ScreenData
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.Model.prueba.UiStopEvent
import com.jonathanev.review.Fragments.Adaptadores.ListCreateImagesAdapter
import com.jonathanev.review.Fragments.Adaptadores.ListCreateTextsAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.SharedFragmentCreateFileViewModel
import com.jonathanev.review.databinding.FragmentCreateFileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentCreateFile : Fragment() {
    private var _binding: FragmentCreateFileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedFragmentCreateFileViewModel by activityViewModels()

    private lateinit var adaptListCreateTexts: ListCreateTextsAdapter
    private lateinit var adaptListCreateImages: ListCreateImagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = BundleCompat.getParcelable(
            requireArguments(), "mode", FolderAction::class.java
        ) ?: FolderAction.NONE

        val screenData = BundleCompat.getParcelable(
            requireArguments(), "screenData", ScreenData::class.java
        ) ?: ScreenData("", "", 0, 0)

        initUI()
        initListeners()
        observers()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStopEvent.collect { uiStopEvent ->
                    if (uiStopEvent is UiStopEvent.ShowMessage) {
                        Toast.makeText(
                            requireContext(),
                            uiStopEvent.text,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    adaptListCreateTexts.submitList(uiState.textList)
                    adaptListCreateImages.submitList(uiState.imageList)
                }
            }
        }
    }

    private fun observers() {
        viewModel.typeContent.observe(viewLifecycleOwner) {
            binding.lblPregResp.text =
                if (it == TypeContent.QUESTION) getString(R.string.etPregunta)
                else getString(R.string.etRespuesta)
        }
    }

    private fun initUI() {
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
            R.id.action_fragmentCreateFile2_to_fragmentCreateImages,
            bundleOf("questionImage" to viewModel.uiState.value.imageList[position])
        )
    }

    private fun goEditText(position: Int) {
        viewModel.setEditingMode(true, position)

        findNavController().navigate(
            R.id.action_fragmentCreateFile2_to_fragmentCreateText,
            bundleOf("questionText" to viewModel.uiState.value.textList[position])
        )
    }

    private fun initListeners() {
        binding.btnAddText.setOnClickListener {
            findNavController().navigate(
                R.id.action_fragmentCreateFile2_to_fragmentCreateText
            )
        }

        binding.btnAddImages.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("posImage", -1)
            }

            findNavController().navigate(
                R.id.action_fragmentCreateFile2_to_fragmentCreateImages,
                bundle
            )
        }

        binding.btnPregResp.setOnClickListener {
            viewModel.rollPregResp()
        }

        binding.btnPrevious.setOnClickListener {
            viewModel.previousQuestion()
        }
    }
}