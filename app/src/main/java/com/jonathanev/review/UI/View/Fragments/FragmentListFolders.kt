package com.jonathanev.review.UI.View.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.jonathanev.review.Core.Constants.BASERUTA_IMG_CIFRADO
import com.jonathanev.review.Core.Constants.GUIAS
import com.jonathanev.review.Core.Constants.IMAGENES
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.FolderResult
import com.jonathanev.review.Data.Model.prueba.FolderUI
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Fragments.Adaptadores.ListFoldersAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.View.Fragments.FragmentDialogNuevoArchivoPopu.DialogListener
import com.jonathanev.review.UI.ViewModel.Fragments.FragDialListarFoldersViewModel
import com.jonathanev.review.UI.ViewModel.Fragments.MainToolbarViewModel
import com.jonathanev.review.databinding.FragmentListFoldersBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

@AndroidEntryPoint
class FragmentListFolders : DialogFragment(), DialogListener {
    private var _binding: FragmentListFoldersBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<FragDialListarFoldersViewModel>()
    private val viewModelToolbar: MainToolbarViewModel by activityViewModels()
    private lateinit var adaptListFolders: ListFoldersAdapter

    @Inject
    lateinit var filePathsProvider: FilePathsProvider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentListFoldersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDialogClosed() {
        // Realizar las acciones necesarias después de cerrar el diálogo
        val dialogActual = dialog
        dialogActual!!.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foldersUiState.collect { uiState ->
                    binding.progressBar.isVisible = uiState.isLoading

                    if (uiState.error != null) {
                        Toast.makeText(requireContext(), uiState.error, Toast.LENGTH_LONG).show()
                    }

                    adaptListFolders.submitList(uiState.folders)
                }
            }
        }

        viewModel.file.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.VISIBLE
            //guiasViewModel.getAllUpdatedGuides(it)
        }

        binding.btnCreateGuide.setOnClickListener {
            findNavController().navigate(
                R.id.action_fragmentDialogListarGuiasPopup_to_fragmentCreateFiles,
                bundleOf("mode" to FolderAction.CREATING_FOLDER)
            )
        }
    }

    private fun initUI() {
        viewModelToolbar.changeTitle("Carpetas")

        binding.progressBar.visibility = View.VISIBLE

        adaptListFolders = ListFoldersAdapter { position -> showFolderOptions(position) }
        binding.lvGuiasEstudioNew.layoutManager = GridLayoutManager(context, 2)
        binding.lvGuiasEstudioNew.setHasFixedSize(true)
        binding.lvGuiasEstudioNew.adapter = adaptListFolders

        viewModel.getAllFolders()
    }

    @SuppressLint("SdCardPath")
    private fun showFolderOptions(position: Int) {
        val folderResult = viewModel.getFolderSelected(position)
        //val a = guiasViewModel.guias.value
        //var fileClickeado: File = filePathsProvider.fileGuides

        when (folderResult) {
            //GuiaResult.Empty -> Log.i("Vacio", "Vacio")
            is FolderResult.Error -> Log.i("Error", "Error")
            is FolderResult.Success -> {
                val builder = AlertDialog.Builder(context)
                builder.setIcon(R.drawable.ic_advertencia)
                builder.setTitle("¿Qué acción deseas realizar?")
                builder.setItems(
                    arrayOf<CharSequence>(
                        "Abrir",
                        "Eliminar",
                        "Cancelar"
                    )
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            viewModel.changeFilePath(folderResult.folder.folderModel.nameFolder)
                            if (folderResult.folder.numGuides == 0) {
                                findNavController().navigate(
                                    R.id.action_fragmentDialogListarGuiasPopup_to_fragmentWithoutFiles,
                                )
                            } else {
                                findNavController().navigate(
                                    R.id.action_fragmentListFolders_to_fragmentListGuides,
                                )
                            }

                            binding.imgvFolder.visibility = View.GONE
                            binding.tvNuevaCarpeta.visibility = View.GONE
                            binding.imgvBack.visibility = View.VISIBLE
                            binding.tvRegresar.visibility = View.VISIBLE
                        }

                        1 -> {
                            // Se ejecuta cuando quiere eliminar la guía.
                            AlertDialog.Builder(context)
                                .setTitle("¡Atención!")
                                .setMessage(
                                    "¿Estás seguro que deseas eliminar la carpeta y su contenido?"
                                )
                                .setPositiveButton("Si") { _, _ ->
                                    deleteFiles(folderResult.folder)
                                }
                                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                                .create().show()
                        }

                        2 -> {
                            // Cuando cancela se ejecuta esta acción
                            dialog.dismiss()
                            Toast.makeText(context, "Cancelaste la acción", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
                builder.create().show()
            }
        }
    }

    // Creating file if it's possible
    private fun existingFile(archivoEnCarpeta: File, onResult: (Boolean) -> Unit) {
        if (!archivoEnCarpeta.exists()) {
            return onResult(true)
        }

        AlertDialog.Builder(context)
            .setTitle("¡Atención!")
            .setMessage(
                ("Ya tienes una guía con el mismo nombre, " +
                        "si continuas se va a sobreescribir la guia, " +
                        "¿seguro deseas continuar?")
            )
            .setPositiveButton(
                "Continuar"
            ) { _, _ ->
                onResult(true)
            }
            .setNegativeButton(
                "Cancelar"
            ) { dialog, _ ->
                dialog.dismiss()
                onResult(false)
            }.create().show()
    }

    private fun relocateGuideWithImages(
        currentPath: String,
        newCurrentPath: File,
        fileName: String,
        folderResult: FolderResult.Success,
        newPathWithoutFile: String
    ) {
        moverImagenes(File(currentPath), fileName, newPathWithoutFile)
        val currentPathWithFile =
            "${filePathsProvider.buildFile(File(currentPath), folderResult.folder.folderModel.nameFolder)}.xml"

        Files.copy(
            Paths.get(currentPathWithFile),
            Paths.get(newCurrentPath.toString()),
            StandardCopyOption.REPLACE_EXISTING
        )

        // Borrar archivo
        File(currentPathWithFile).delete()
        //guiasViewModel.getAllUpdatedGuides(filePathsProvider.fileGuides)
        viewModel.getFirstPath()

        Toast.makeText(
            context,
            "El archivo se movió correctamente",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun deleteFiles(folderResult: FolderUI) {
        if (!viewModel.existFolder(folderResult.folderModel.nameFolder)) {
            Toast.makeText(
                context,
                "La ruta para eliminar la carpeta actualmente no existe.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val message = viewModel.deleteFiles(folderResult)

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val metrics = resources.displayMetrics

            val maxWidth = 900   // en píxeles
            val maxHeight = 1200 // en píxeles

            val calculatedWidth = (metrics.widthPixels * 0.9).toInt()  // 90% del ancho de pantalla
            val calculatedHeight = (metrics.heightPixels * 0.8).toInt() // 80% del alto de pantalla

            val finalWidth = minOf(calculatedWidth, maxWidth)
            val finalHeight = minOf(calculatedHeight, maxHeight)

            window.setLayout(finalWidth, finalHeight)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun moverImagenes(currentPath: File, fileName: String, newPathWithoutFile: String) {
        //obtenerDatosXML(fileClickeado, selectedFolder)
        //var doc: Document? = null
        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder

        try {
            db = dbf.newDocumentBuilder()
            val filePath: File = File(currentPath.toString() + fileName)
            val fis = FileInputStream(filePath)

            val doc = db.parse(fis)

            // Buscamos los Nodos Interrogante y accedemos a lo que se encuentre dentro.
            val cuestionario: NodeList = doc.getElementsByTagName("Interrogante")
            for (i in 0 until cuestionario.length) {
                // Accedes a los elementos de dicho nodo
                val e: Element = cuestionario.item(i) as Element

                val value = mapOf(
                    "respuesta" to e.getAttribute("respuesta"),
                    "pregunta" to e.getAttribute("pregunta")
                ).filterValues { it.contains(BASERUTA_IMG_CIFRADO) }

                if (value.isNotEmpty()) {
                    value.keys.forEach { pathImage ->
                        //var descifrado = cifrar(pathImage, 26 - 3)
                        // descifrado = descifrado.replace("content://media/picker/".toRegex(), "")
                        val image = pathImage.substringAfterLast("/")
                        val ruta = currentPath.toString().replace(GUIAS, IMAGENES)
                        val pathImage = "$newPathWithoutFile/$image"

                        Files.copy(
                            Paths.get(ruta + image),
                            Paths.get("" + pathImage),
                            StandardCopyOption.REPLACE_EXISTING
                        )

                        File(ruta + image).delete()
                    }
                }
            }
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun cifrar(texto: String, desplazamiento: Int): String {
        val resultado = StringBuilder()

        for (caracter in texto) {
            if (caracter.isLetter()) {
                val base = if (caracter.isUpperCase()) 'A' else 'a'
                val letraCifrada = ((caracter - base + desplazamiento) % 26 + base.code).toChar()
                resultado.append(letraCifrada)
            } else {
                resultado.append(caracter)
            }
        }

        return resultado.toString()
    }
}