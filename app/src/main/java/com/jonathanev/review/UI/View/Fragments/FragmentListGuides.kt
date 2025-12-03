package com.jonathanev.review.UI.View.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathanev.review.Data.FolderAction
import com.jonathanev.review.Data.Model.GuideResult
import com.jonathanev.review.Fragments.Adaptadores.ListGuidesAdapter
import com.jonathanev.review.R
import com.jonathanev.review.UI.ViewModel.Fragments.FragmentListGuidesViewModel
import com.jonathanev.review.databinding.FragmentListGuidesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentListGuides : Fragment() {
    private var _binding: FragmentListGuidesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<FragmentListGuidesViewModel>()
    private lateinit var adaptListGuides: ListGuidesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListGuidesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initListeners()

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

        viewModel.guides.observe(viewLifecycleOwner){ guides ->
            adaptListGuides.submitList(guides)
        }
    }

    private fun initListeners() {
        binding.btnCreateGuide.setOnClickListener {
            findNavController().navigate(
                R.id.action_fragmentListGuides_to_fragmentCreateFiles2,
                bundleOf("mode" to FolderAction.CREATING_FILE)
            )
        }
    }

    private fun initUI() {
        adaptListGuides = ListGuidesAdapter { position -> showGuideOptions(position) }
        binding.lvGuiasEstudioNew.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.lvGuiasEstudioNew.setHasFixedSize(true)
        binding.lvGuiasEstudioNew.adapter = adaptListGuides

        viewModel.getAllGuides()
    }

    private fun showGuideOptions(position: Int) {
        val guideResult = viewModel.getGuideSelected(position)

        when(guideResult){
            is GuideResult.Error -> Log.i("Error", "Error")
            is GuideResult.Success -> {
                val builder = AlertDialog.Builder(context)
                builder.setIcon(R.drawable.ic_advertencia)
                builder.setTitle("¿Qué acción deseas realizar?")
                builder.setItems(
                    arrayOf<CharSequence>(
                        "Abrir",
                        "Modificar",
                        "Eliminar",
                        "Cambiar nombre",
                        "Mover a",
                        "Cancelar"
                    )
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            viewModel.changeFilePath(guideResult.folder.nameGuide)
                            findNavController().navigate(
                                R.id.action_fragmentListGuides_to_fragmentPreviewQuestions,
                            )
                        }

                        1 -> {
                            /*// Si entra al segundo es para modificar la guía de estudio
                            viewModel.changeFilePath(folderResult.folder.nombreGuia)

                            val intentActivityModificarGuia =
                                Intent(activity, ActivityModificar::class.java)
                            //intentModificarGuia.putExtra("nombre_archivo", guia.nombreGuia)
                            //intentActivityModificarGuia.putExtra("ruta", ruta)
                            startActivity(intentActivityModificarGuia)
                            // Recuperamos el dialogo abierto actualmente
                            // (Fragment_DialogListarGuias.java) y lo cerramos.
                            val dialogoModificarGuia = getDialog()
                            dialogoModificarGuia!!.dismiss()*/
                        }

                        2 ->
                            // Se ejecuta cuando quiere eliminar la guía.
                            AlertDialog.Builder(context)
                                .setTitle("¡Atención!")
                                .setMessage(
                                    "¿Estás seguro que deseas eliminar la" +
                                            " guia?"
                                )
                                .setPositiveButton("Si") { _, _ ->
                                    //deleteFiles(folderResult.folder)

                                    //guiasViewModel.changeFilePath(guiaResult.guia.nombreGuia)
                                    //val route = guiasViewModel.getCurrentPath()
                                    // Si entra al tercero es para eliminar la guia exitosamente
                                    /*if (filePathsProvider.fileGuides.exists()) {
                                        File(route).delete()
                                        Toast.makeText(
                                            context,
                                            "¡Archivo eliminado exitosamente!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        //guiasViewModel.getAllUpdatedGuides(filePathsProvider.fileGuides)

                                        // Recuperamos el dialogo abierto actualmente
                                        // (Fragment_DialogListarGuias_popup.java)
                                        // y lo cerramos.
                                        val dialogoEliminarGuia = getDialog()
                                        dialogoEliminarGuia!!.dismiss()
                                    } else {
                                        Toast.makeText(
                                            context, "La ruta para eliminar el " +
                                                    "archivo actualmente no existe.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }*/
                                }
                                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                                .create().show()
                        3 -> {
                            /*// Se ejecuta cuando quiere cambiar el nombre de la guía
                            viewModel.changeFilePath(folderResult.folder.nombreGuia)
                            val dialogo = FragmentDialogNuevoArchivoPopu.newInstance(
                                mode = FolderAction.RENAMING_FILE
                            )
                            dialogo.show(childFragmentManager, "Fragment_nuevo")*/
                        }

                        4 -> {
                            /*val subMenuBuilder = AlertDialog.Builder(context)
                            subMenuBuilder.setTitle("Mover a...")

                            val foldersCreated = viewModel.getFoldersCreated()
                            val currentPath = viewModel.getCurrentPath()

                            subMenuBuilder.setItems(foldersCreated) { _, subWhich ->
                                // Manejar la selección de la carpeta dentro del submenú
                                val selectedFolder = foldersCreated[subWhich]

                                // Mover a la carpeta seleccionada
                                try {
                                    /*// Copiar el archivo
                                    val guia = guiasViewModel.getGuia(position)*/

                                    // Updated path
                                    val fileName = "${folderResult.folder.nombreGuia}.xml"
                                    val archivoEnCarpeta = filePathsProvider.buildFileFolder(
                                        File(filePathsProvider.fileGuides.toString()),
                                        selectedFolder,
                                        fileName
                                    )
                                    var creatingFile = false
                                    existingFile(archivoEnCarpeta) { creatingFile = it }

                                    try {
                                        val newPathWithoutFile: String =
                                            archivoEnCarpeta.toString().substringBeforeLast("/")
                                        var newPath: File = File(newPathWithoutFile)
                                        if (selectedFolder == PRINCIPAL) {
                                            newPath = filePathsProvider.fileGuides
                                        }

                                        if (creatingFile) {
                                            relocateGuideWithImages(
                                                currentPath,
                                                newPath,
                                                fileName,
                                                folderResult,
                                                newPathWithoutFile
                                            )
                                        }

                                        /*guiasViewModel.getFirstPath()
                                        guiasViewModel.getAllUpdatedGuides(
                                            filePathsProvider.fileGuides
                                        )*/

                                        binding.imgvFolder.visibility = View.VISIBLE
                                        binding.tvNuevaCarpeta.visibility = View.VISIBLE
                                        binding.imgvBack.visibility = View.GONE
                                        binding.tvRegresar.visibility = View.GONE

                                        Toast.makeText(
                                            context,
                                            "El archivo se movió correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: Exception) {
                                        println("Error al copiar el archivo: ${e.message}")
                                    }

                                } catch (e: Exception) {
                                    println("Error al copiar el archivo: ${e.message}")
                                }
                            }

                            subMenuBuilder.show()*/
                        }

                        5 -> {
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
}