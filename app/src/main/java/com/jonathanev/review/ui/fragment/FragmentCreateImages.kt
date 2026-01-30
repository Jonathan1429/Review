package com.jonathanev.review.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.davemorrissey.labs.subscaleview.ImageSource
import com.jonathanev.review.presentation.viewmodel.MainToolbarViewModel
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.databinding.FragmentCreateImagesBinding
import com.jonathanev.review.presentation.model.QuestionContentUi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentCreateImages : Fragment() {
    private var _binding: FragmentCreateImagesBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedFragmentCreateFileViewModel by activityViewModels()
    private val viewModelToolbar: MainToolbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arguments?.getParcelable(
            "questionImage",
            QuestionContentUi.Image::class.java
        ) ?: QuestionContentUi.None

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModelToolbar.onSave.collect {
                        saveProcess()
                    }
                }

                launch {
                    viewModelToolbar.onBefore.collect {
                        findNavController().navigateUp()
                    }
                }
            }
        }

        initUI(data)
        initListeners()
    }

    private fun saveProcess() {
        if (!binding.ivImagen.isImageLoaded) {
            Toast.makeText(requireContext(), "Debes asignar una Imagen", Toast.LENGTH_LONG).show()
            return
        }

        processImage()

        findNavController().navigateUp()
    }

    private fun initUI(item: QuestionContentUi) {
        if (item is QuestionContentUi.Image){
            binding.ivImagen.setImage(ImageSource.uri(item.uri))
        }

        viewModelToolbar.isBtnSaveVisible(true)
        viewModelToolbar.isBtnBackVisible(true)
    }

    private fun initListeners() {
        binding.fabAddImage.setOnClickListener {
            openSomeActivityForResult()
        }
    }

    // Si funciona pasaremos a probar los botones de siguiente y atrás
    private fun openSomeActivityForResult() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                onImagePicked(uri)
            }
        }

    private fun onImagePicked(uri: Uri) {
        // Mostrar imagen sin crear archivo
        binding.ivImagen.setImage(ImageSource.uri(uri))
        // Guardas el URI para usarlo después al guardar todo
        sharedViewModel.setActualUri(uri.toString())
        //processImage(uri)
    }

    private fun processImage() {
        // Editing
        sharedViewModel.addImageContent()
    }
}