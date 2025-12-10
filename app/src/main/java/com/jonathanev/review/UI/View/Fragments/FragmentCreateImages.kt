package com.jonathanev.review.UI.View.Fragments

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
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.UI.ViewModel.Fragments.MainToolbarViewModel
import com.jonathanev.review.UI.ViewModel.Fragments.SharedFragmentCreateFileViewModel
import com.jonathanev.review.databinding.FragmentCreateImagesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentCreateImages : Fragment() {
    private var _binding: FragmentCreateImagesBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedFragmentCreateFileViewModel by activityViewModels()
    private val sharedToolbarViewModel: MainToolbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val posImage = arguments?.getInt(
            "posImage"
        ) ?: -1

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedToolbarViewModel.onSave.collect {
                    saveProcess(posImage)
                }
            }
        }

        initUI(posImage)
        initListeners()
    }

    private fun saveProcess(posImage: Int) {
        if (!binding.ivImagen.isImageLoaded) {
            Toast.makeText(requireContext(), "Debes asignar una Imagen", Toast.LENGTH_LONG).show()
            return
        }

        val uri = sharedViewModel.getActualUri()
        processImage(uri, posImage)

        findNavController().navigateUp()
    }

    private fun initUI(posImage: Int) {
        if (posImage != -1) {
            val mutableRefList = sharedViewModel.getActualList()
            val count = sharedViewModel.getCount()

            when (val actualItem = mutableRefList[count].content[posImage]) {
                is QuestionContent.Image -> binding.ivImagen.setImage(ImageSource.uri(actualItem.decodedPath))
                QuestionContent.None -> Unit
                is QuestionContent.Text -> Unit
            }
        }

        sharedToolbarViewModel.isBtnSaveVisible(View.VISIBLE)
        sharedToolbarViewModel.isBtnBackVisible(View.VISIBLE)
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
        sharedViewModel.setActualUri(uri)
        //processImage(uri)
    }

    private fun processImage(uri: Uri, posImage: Int) {
        if (posImage != -1) {
            sharedViewModel.replaceUriImages(uri, posImage)
            return
        }
        sharedViewModel.addUriImagesSelected(uri)
    }

    fun guardarTemporalmente() {

    }

    fun guardarImagenesFinal() {
        /*for ((index, uri) in viewModel.imagenesUris.withIndex()) {

            val input = requireContext().contentResolver.openInputStream(uri)
                ?: continue

            val filename = "imagen_$index.png"
            val outFile = File(requireContext().filesDir, filename)

            input.use { i ->
                FileOutputStream(outFile).use { o ->
                    i.copyTo(o)
                }
            }

            // Aquí puedes cifrar o registrar la ruta final
            // viewModel.registrarImagenGuardada(outFile.absolutePath)
        }*/
    }
}